package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.ChefRankAnalyticsDTO;
import it.unipi.MySmartRecipeBook.dto.YearAnalyticsDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.dto.users.PendingChefDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserInfoDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.AdminPendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import it.unipi.MySmartRecipeBook.model.Mongo.users.PendingChef;
import it.unipi.MySmartRecipeBook.repository.Mongo.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.ChefNeo4jRepository;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import it.unipi.MySmartRecipeBook.event.Task;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.unipi.MySmartRecipeBook.dto.TrendAnalyticsDTO;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;

/**
 * Admin service that handles admin's business logic operations
 */
@Service
public class AdminService {

    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeAdmin;

    private final RecipeUtilityFunctions recipeConversions;
    private final ChefRepository chefRepository;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeRepository;
    private final LowLoadManager lowLoadManager;
    private final FoodieRepository foodieRepository;
    private final ChefNeo4jRepository chefNeo4jRepository;
    private final ChefUtilityFunctions chefUtilityFunctions;

    public AdminService(RecipeUtilityFunctions recipeConversions, ChefRepository chefRepository,
                        AdminRepository adminRepository, RecipeMongoRepository recipeRepository,
                        LowLoadManager lowLoadManager, FoodieRepository foodieRepository, ChefNeo4jRepository chefNeo4jRepository, ChefUtilityFunctions chefUtilityFunctions) {
        this.recipeConversions = recipeConversions;
        this.chefRepository = chefRepository;
        this.adminRepository = adminRepository;
        this.recipeRepository = recipeRepository;
        this.lowLoadManager = lowLoadManager;
        this.foodieRepository = foodieRepository;
        this.chefNeo4jRepository = chefNeo4jRepository;
        this.chefUtilityFunctions = chefUtilityFunctions;
    }


     /**
     * Approves a pending recipe, changing its status from "PENDING" to "APPROVED".
     * The recipe is removed from the admin's list of pending recipes and the corresponding chef's list,
     * and is then added to the new recipes list. Finally, it is asynchronously added to the graph database.
     * @param recipeId the unique identifier of the recipe to be approved
     * @throws NoSuchElementException if the admin or the recipe is not found in the database,
     * or if the recipe is not in a "PENDING" state
     */
    @Transactional
    public void saveRecipe(String recipeId) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        RecipeMongo recipeToModify = recipeRepository.findById(recipeId)
                        .orElseThrow(()-> new NoSuchElementException("Recipe not found"));

        if(!recipeToModify.getStatus().equals("PENDING")) {
            throw new NoSuchElementException("Recipe not found");
        }

        recipeToModify.setStatus("APPROVED");
        recipeRepository.save(recipeToModify);

        adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId);
        addToChefRecipes(recipeToModify);

        GraphRecipeDTO graphRecipe = recipeConversions.MongoToNeo4jGraph(recipeToModify);
        lowLoadManager.addTask(Task.TaskType.CREATE_RECIPE_NEO4J, graphRecipe);

    }


    /**
     * Removes an approved recipe from the chef's pending recipes list and adds it to the new_recipes list.
     * Ensures that the new_recipes list contains a maximum of 15 elements, moving the oldest one to the
     * old_recipes list of IDs if the limit is exceeded.
     * @param recipe the approved {@link RecipeMongo} entity to add
     * @throws NoSuchElementException if the corresponding chef is not found
     */
    private void addToChefRecipes(RecipeMongo recipe) {

        String chefId = recipe.getChef().getId();

        Chef chef = chefRepository.findApprovedById(chefId)
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if (chef.getRecipesToConfirm() != null) {
            chef.getRecipesToConfirm().removeIf(pendingRecipe -> pendingRecipe.getId().equals(recipe.getId()));
        }

        ChefRecipeSummary newChefRecipe = recipeConversions.recipeToChefRecipe(recipe);

        if(chef.getNewRecipes() == null) {
            chef.setNewRecipes(new java.util.ArrayList<>());
        }

        chef.getNewRecipes().add(0, newChefRecipe);

        if( chef.getNewRecipes().size() > 15 ) {
            ChefRecipeSummary oldestRecipe = chef.getNewRecipes().remove(15);

            if(chef.getOldRecipes() == null) {
                chef.setOldRecipes(new java.util.ArrayList<>());
            }
            chef.getOldRecipes().add(0, oldestRecipe.getId());
        }


        int totalRecipes = chef.getTotalRecipes() != null ? chef.getTotalRecipes() : 0;
        chef.setTotalRecipes(totalRecipes + 1);
        chefRepository.save(chef);
    }


    /**
     * Rejects a pending recipe, removing it from both the admin's approval list and the corresponding chef's waiting list.
     * The recipe is then permanently deleted from the database.
     * @param recipeId the unique identifier of the recipe to discard
     * @throws NoSuchElementException if the admin or the recipe is not found, or if the recipe is already approved
     */
    @Transactional
    public void discardRecipe(String recipeId) {
        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        RecipeMongo recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        if(recipe.getStatus().equals("APPROVED")) {
            throw new NoSuchElementException("Recipe not found");
        }

        String chefId = recipe.getChef().getId();

        boolean recipeFoundAdmin = adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId) > 0;

        if(recipeFoundAdmin) {
            chefRepository.removeRecipeFromWaiting(chefId, recipeId);
        }

        recipeRepository.deleteById(recipeId);
    }


    /**
     * Approves a pending chef registration request. The chef is removed from the admin's pending list,
     * its status is changed to "APPROVED", and the corresponding node is added to the graph database.
     * @param chefUsername the unique username of the chef to approve
     * @throws NoSuchElementException if the admin or the chef is not found, or if the chef's status is already "APPROVED"
     */
    @Transactional
    public void approveChef(String chefUsername) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        Chef chef = chefRepository.findByUsername(chefUsername)
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if(chef.getStatus().equals("APPROVED")) {
            throw new NoSuchElementException("Chef to approve not found");
        }

        chef.setStatus("APPROVED");
        chefRepository.save(chef);

        adminRepository.removeChefFromApprovals(admin.getId(), chefUsername);

        ChefNeo4j chefNeo4j = new ChefNeo4j();
        chefNeo4j.setMongoId(chef.getId());
        chefNeo4j.setName(chef.getName());
        chefNeo4j.setSurname(chef.getSurname());
        chefNeo4jRepository.save(chefNeo4j);
    }


    /**
     * Rejects a pending chef registration request. The chef is removed from the admin's pending list
     * and from the chef collection.
     * @param chefUsername the unique username of the chef to reject
     * @throws NoSuchElementException if the admin or the chef is not found, or if the chef's status is already "APPROVED"
     */
    @Transactional
    public void declineChef(String chefUsername) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        Chef chefToDiscard = chefRepository.findByUsername(chefUsername)
                .orElseThrow(() -> new NoSuchElementException("Chef to discard not found"));

        if(chefToDiscard.getStatus().equals("APPROVED")) {
            throw new NoSuchElementException("Chef to discard not found");
        }

        chefRepository.deleteById(chefToDiscard.getId());
        adminRepository.removeChefFromApprovals(admin.getId(), chefUsername);

    }


    /**
     * Retrieves a paginated list of pending recipes waiting for the admin's approval.
     * @param pageNumber the requested page number
     * @return a {@link SliceRecipeDTO} containing the requested page of {@link PendingRecipeDTO}s,
     * along with two boolean values indicating the existence of previous or next pages
     * @throws NoSuchElementException if the admin is not found
     * @throws IllegalArgumentException if the pageNumber is negative
     */
    public SliceRecipeDTO<PendingRecipeDTO> showPendingRecipes(int pageNumber) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        if (admin.getRecipesToApprove() == null || admin.getRecipesToApprove().isEmpty()) {
            return new SliceRecipeDTO<>(null, false, false);
        }

        List<AdminPendingRecipe> adminPendingRecipes = admin.getRecipesToApprove();

        int start = (pageNumber - 1) * pageSizeAdmin;
        int end = Math.min(pageNumber * pageSizeAdmin, adminPendingRecipes.size());

        if (start >= adminPendingRecipes.size()) {
            return new SliceRecipeDTO<>(null, false, true);
        }

        List<PendingRecipeDTO> content = new ArrayList<>();
        for (AdminPendingRecipe recipe : adminPendingRecipes.subList(start, end)) {
            content.add(recipeConversions.pendingRecipeToAdminDTO(recipe));
        }

        boolean hasPrevious = pageNumber > 1;
        boolean hasNext = adminPendingRecipes.size() > end;

        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }


    /**
     * Retrieves a paginated list of pending chef request waiting for the admin's approval.
     * @param pageNumber the requested page number
     * @return a {@link SliceRecipeDTO} containing the requested page of {@link PendingChefDTO}s,
     * along with two boolean values indicating the existence of previous or next pages
     * @throws NoSuchElementException if the admin is not found
     * @throws IllegalArgumentException if the pageNumber is negative
     */
    public SliceRecipeDTO<PendingChefDTO> showPendingChefs(int pageNumber) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        if (admin.getChefsToApprove() == null || admin.getChefsToApprove().isEmpty()) {
            return new SliceRecipeDTO<>(null, false, false);
        }

        List<PendingChef> pendingChefs = admin.getChefsToApprove();

        int start = (pageNumber - 1) * pageSizeAdmin;
        int end = Math.min(pageNumber * pageSizeAdmin, pendingChefs.size());

        if (start >= pendingChefs.size()) {
            return new SliceRecipeDTO<>(null, false, true);
        }

        List<PendingChefDTO> content = chefUtilityFunctions.PendingChefListToDTO(
                pendingChefs.subList(start, end));

        boolean hasPrevious = pageNumber > 1;
        boolean hasNext = pendingChefs.size() > end;

        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }


    /**
     * Retrieves the detailed information of a chef waiting for the admin approval.
     * @param chefUsername unique username of the chef to visualize
     * @return a {@link RegisteredUserInfoDTO} containing the detailed profile information of the requested chef
     * @throws NoSuchElementException if the admin or the chef is not found, if the chef is not
     * present in the admin's pending list, or if the chef has already been approved
     */
    public RegisteredUserInfoDTO seeChefDetails(String chefUsername){

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        boolean found = admin.getChefsToApprove().stream()
                .anyMatch(chef -> chef.getUsername().equals(chefUsername));

        if(!found) {
            throw new NoSuchElementException("Chef to approve not found");
        }

        Chef chef = chefRepository.findByUsername(chefUsername)
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if(chef.getStatus().equals("APPROVED")) {
            throw new NoSuchElementException("Chef not found");
        }

        return chefUtilityFunctions.pendingChefToChefDetails(chef);
    }

    /**
     * Retrieves the detailed information of a recipe waiting for the admin approval.
     * @param recipeId unique identifier of the recipe to visualize
     * @return a {@link ShowRecipeDTO} containing the detailed information of the requested recipe
     * @throws NoSuchElementException if the admin or the recipe is not found, if the recipe is not
     * present among the one waiting for the admin's approval, or if the recipe has already been approved
     */
    public ShowRecipeDTO seeRecipeDetails(String recipeId){

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        boolean found = admin.getRecipesToApprove().stream()
                .anyMatch(recipe -> recipe.getId().equals(recipeId));

        if (!found) {
            throw new NoSuchElementException("Recipe not found among admin pending recipes");
        }

        RecipeMongo recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        if(!recipe.getStatus().equals("PENDING")){
            throw new NoSuchElementException("Recipe not found");
        }

        return recipeConversions.entityToDto(recipe);
    }


    /**
     * Retrieves registration statistics for foodies.
     * The data is grouped by year and includes a breakdown of new registrations per month.
     * @return a list of {@link YearAnalyticsDTO} containing the registration counts grouped by year and month
     */
    public YearAnalyticsDTO getMonthlyFoodies(int year) {

        int currentYear = Year.now().getValue();
        if(year >= currentYear || year < 2015) {
            throw new IllegalArgumentException("Invalid year");
        }
        return foodieRepository.getMonthlyFoodiesStats(year);
    }


    /**
     * Analyzes and retrieves the trends of recipe categories based on their creation dates.
     * Compares the volume of recipes created in the last year against the previous year to calculate
     * a percentage growth rate. Classifies each category's trend as "NEW", "EMERGING", "DECLINING", or "STABLE".
     * @return a list of {@link TrendAnalyticsDTO} containing the classified trends and growth percentages
     */
    public List<TrendAnalyticsDTO> getCategoryTrends() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneYearAgo = now.minusYears(1);
        LocalDateTime twoYearsAgo = now.minusYears(2);

        List<TrendAnalyticsDTO> results =
                recipeRepository.findCategoryTrend(oneYearAgo, twoYearsAgo);

        for (TrendAnalyticsDTO dto : results) {
            if (dto.getGrowthRate() != null) {
                    dto.setGrowthRate(dto.getGrowthRate() * 100);
            }
            if (dto.getPreviousCount() == 0 && dto.getRecentCount() > 0) {
                dto.setTrendType("NEW");
            } else if (dto.getGrowthRate() != null && dto.getGrowthRate() > 0) {
                dto.setTrendType("EMERGING");
            } else if (dto.getGrowthRate() != null && dto.getGrowthRate() < 0) {
                dto.setTrendType("DECLINING");
            } else {
                dto.setTrendType("STABLE");
            }
        }

        return results;
    }


    /**
     * Calculates and retrieves the ranking of chefs using a Bayesian average scoring system.
     * @return a list of {@link ChefRankAnalyticsDTO} containing the ranked chefs, their scores, and their positions
     */
    public List<ChefRankAnalyticsDTO> getBayesianRanking() {
        return chefRepository.chefBayesianRanking();
    }

}


