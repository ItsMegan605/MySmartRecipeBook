package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.ChefRankAnalyticsDTO;
import it.unipi.MySmartRecipeBook.dto.YearAnalyticsDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.PendingRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.PendingChefDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.OldRecipe;
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
//import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.unipi.MySmartRecipeBook.dto.TrendAnalyticsDTO;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Admin service that handles admin's business logic operations
 */
@Service
public class AdminService {

    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeAdmin;

    private final RecipeUtilityFunctions recipeConvertions;
    private final ChefRepository chefRepository;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeRepository;
    private final LowLoadManager lowLoadManager;
    private final FoodieRepository foodieRepository;
    private final ChefNeo4jRepository chefNeo4jRepository;
    private final ChefUtilityFunctions chefUtilityFunctions;

    public AdminService(RecipeUtilityFunctions recipeConvertions, ChefRepository chefRepository,
                        AdminRepository adminRepository, RecipeMongoRepository recipeRepository,
                        LowLoadManager lowLoadManager, FoodieRepository foodieRepository, ChefNeo4jRepository chefNeo4jRepository, ChefUtilityFunctions chefUtilityFunctions) {
        this.recipeConvertions = recipeConvertions;
        this.chefRepository = chefRepository;
        this.adminRepository = adminRepository;
        this.recipeRepository = recipeRepository;
        this.lowLoadManager = lowLoadManager;
        this.foodieRepository = foodieRepository;
        this.chefNeo4jRepository = chefNeo4jRepository;
        this.chefUtilityFunctions = chefUtilityFunctions;
    }

    /**
     * Approve a pending recipe
     * @param recipeId - recipe id
     */
    @Transactional
    public void saveRecipe(String recipeId) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        //Get the pending recipe's list and we look for the specified id
        List<AdminPendingRecipe> recipesToApprove = admin.getRecipesToApprove();

        if (recipesToApprove == null) {
            throw new NoSuchElementException("No recipe has to be approved");
        }

        AdminPendingRecipe recipeApproved = null;
        for (AdminPendingRecipe recipe : recipesToApprove) {
            if (recipe.getId().equals(recipeId)) {
                recipeApproved = recipe;
                break;
            }
        }

        if (recipeApproved == null) {
            throw new NoSuchElementException("Recipe not found among the ones that have to be approved");
        }

        if (recipeRepository.existsByTitle(recipeApproved.getTitle())) {
            throw new DataIntegrityViolationException("Recipe already exists");
        }


        RecipeMongo recipe = recipeConvertions.baseToMongoRecipe(recipeApproved);
        RecipeMongo savedRecipe = recipeRepository.save(recipe);

        addToChefRecipes(savedRecipe, recipeId);
        adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId);

        GraphRecipeDTO graphRecipe = recipeConvertions.MongoToNeo4jGraph(savedRecipe);
        lowLoadManager.addTask(Task.TaskType.CREATE_RECIPE_NEO4J, graphRecipe);

    }

    /**
     * Adds a recipe to chef's page
     * @param recipe - the recipe
     * @param pendingRecipeId - the id of the pending recipe
     */
    private void addToChefRecipes(RecipeMongo recipe, String pendingRecipeId) {

        String chefId = recipe.getChef().getId();

        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if (chef.getRecipesToConfirm() != null) {
            chef.getRecipesToConfirm().removeIf(pending -> pending.getId().equals(pendingRecipeId));
        }
        ChefRecipeSummary newChefRecipe = recipeConvertions.recipeToChefRecipe(recipe);

        if(chef.getNewRecipes() == null) {
            chef.setNewRecipes(new java.util.ArrayList<>());
        }

        chef.getNewRecipes().add(0, newChefRecipe);

        if( chef.getNewRecipes().size() > 15 ) {
            ChefRecipeSummary oldestRecipe = chef.getNewRecipes().remove(14);
            OldRecipe oldRecipe = new OldRecipe(oldestRecipe.getId(), oldestRecipe.getNumSaves());
            chef.getOldRecipes().add(0, oldRecipe);
        }


        int totalRecipes = chef.getTotalRecipes() != null ? chef.getTotalRecipes() : 0;
        chef.setTotalRecipes(totalRecipes + 1);
        chefRepository.save(chef);
    }



    /**
     * Discard a pending recipe
     * @param recipeId - id of the recipe
     */
    @Transactional
    public void discardRecipe(String recipeId) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        List<AdminPendingRecipe> recipesToApprove = admin.getRecipesToApprove();

        if (recipesToApprove == null) {
            throw new NoSuchElementException("No recipe has to be approved");
        }

        String chefId = null;
        for (AdminPendingRecipe recipe : recipesToApprove) {
            if (recipe.getId().equals(recipeId)) {
                chefId = recipe.getChef().getId();
                break;
            }
        }

        if (chefId == null) {
            throw new NoSuchElementException("Recipe not found among the ones that have to be approved");
        }
        boolean recipeFoundAdmin = adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId) > 0;

        if(recipeFoundAdmin) {
            //ObjectId chefObjectId = new ObjectId(chefId);
            chefRepository.removeRecipeFromWaiting(chefId, recipeId);
        }
    }



    /**
     * Approve a pending chef registration request
     * @param chefUsername - username of the chef
     */
    @Transactional
    public void approveChef(String chefUsername) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        List<PendingChef> chefToApprove = admin.getChefsToApprove();
        if (chefToApprove == null) {
            throw new NoSuchElementException("No chef has to be approved");
        }

        PendingChef chef = null;
        for (PendingChef approvedChef : chefToApprove) {
            if (approvedChef.getUsername().equals(chefUsername)) {
                chef = approvedChef;
                break;
            }
        }

        if (chef == null) {
            throw new NoSuchElementException("Chef to approve not found");
        }

        Chef chefMongo = chefUtilityFunctions.pendingChefToChef(chef);

        Chef chefApproved = chefRepository.save(chefMongo);

        adminRepository.removeChefFromApprovals(admin.getId(), chefUsername);

        ChefNeo4j chefNeo4j = new ChefNeo4j();
        chefNeo4j.setMongoId(chefApproved.getId());
        chefNeo4j.setName(chef.getName());
        chefNeo4j.setSurname(chef.getSurname());
        chefNeo4jRepository.save(chefNeo4j);
    }


    /**
     * Discard a pending chef registration request
     * @param chefUsername - chef's username
     */
    public void declineChef(String chefUsername) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        List<PendingChef> chefToApprove = admin.getChefsToApprove();
        if (chefToApprove == null) {
            throw new NoSuchElementException("No chef has to be approved");
        }

        PendingChef chef = null;
        for (PendingChef newChef : chefToApprove) {
            if (newChef.getUsername().equals(chefUsername)) {
                chef = newChef;
                break;
            }
        }

        if (chef == null) {
            throw new NoSuchElementException("Chef to approve not found");
        }

        adminRepository.removeChefFromApprovals(admin.getId(), chefUsername);
    }

    /**
     * Method to show the list of the pending recipes to be approved or discarded
     * @param pageNumber - paging
     * @return the paging with the list of recipes
     */

    public SliceRecipeDTO showPendingRecipes(int pageNumber) {

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
            content.add(recipeConvertions.pendingRecipeToAdminDTO(recipe));
        }

        boolean hasPrevious = pageNumber > 1;
        boolean hasNext = adminPendingRecipes.size() > end;

        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }

    /**
     * Method to show the list of the pending chefs
     * @param pageNumber - paging
     * @return the page with the list of pending chefs
     */
    public SliceRecipeDTO showPendingChefs(int pageNumber) {

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
     * Monthly foodies count
     * @return the monthly foodies subscribed to the app
     */
    public List<YearAnalyticsDTO> getMonthlyFoodies() {
        return foodieRepository.getMonthlyFoodiesStats();
    }


    /**
     * Emerging vs Declining Parameters (Analytics)
     * @return the list of the category trends
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
     * Method for the chef's Bayesian ranking
     * @return the Bayesian Ranking of the chefs
     */
    public List<ChefRankAnalyticsDTO> getBayesianRanking() {
        return chefRepository.chefBayesianRanking();
    }

}


