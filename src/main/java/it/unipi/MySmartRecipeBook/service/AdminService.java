package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.YearAnalyticsDTO;
import it.unipi.MySmartRecipeBook.dto.PopularIngredientsDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.OldRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import it.unipi.MySmartRecipeBook.model.Mongo.users.PendingChef;
import it.unipi.MySmartRecipeBook.repository.Mongo.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.ChefNeo4jRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.utils.convertionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.convertionFunctions.RecipeUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.parameters.Task;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;

import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.unipi.MySmartRecipeBook.dto.TrendAnalyticsDTO;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    /* filtro per gli ingredienti degli chef */

    private static final List<String> COMMON_INGREDIENTS = Arrays.asList( //funzione suggerita da geminiperchè non sapevo come mapparlo
            "salt", "water", "pepper", "baking soda",
            "baking powder", "olive oil", "oil"
    );

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

    /*------------------- Approve a pending recipe  --------------------*/

    @Transactional
    public void saveRecipe(String recipeId) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));


        // Prendiamo l'elenco delle ricette in attesa di approvazione che abbiamo dentro l'admin e cerchiamo quella che
        // ha l'id indicato
        List<PendingRecipe> recipesToApprove = admin.getRecipesToApprove();

        if (recipesToApprove == null) {
            throw new RuntimeException("No recipe has to be approved");
        }

        PendingRecipe recipeApproved = null;
        for (PendingRecipe recipe : recipesToApprove) {
            if (recipe.getId().equals(recipeId)) {
                recipeApproved = recipe;
                break;
            }
        }

        if (recipeApproved == null) {
            throw new RuntimeException("Recipe not found among the ones that have to be approved");
        }


        // Non vogliamo inserire una nuova ricetta che ha lo stesso titolo di un'altra
        if (recipeRepository.existsByTitle(recipeApproved.getTitle())) {
            throw new RuntimeException("Recipe already exists");
        }

        // Quando l'admin approva una ricetta dobbiamo:

        // 1_ Inserire la ricetta in Mongo così da avere anche l'id da inserire nella collezione dello chef
        RecipeMongo recipe = recipeConvertions.baseToMongoRecipe(recipeApproved);
        RecipeMongo savedRecipe = recipeRepository.save(recipe);

        // 2_ Inserire la ricetta tra l'elenco di quelle scritte dallo chef, nalla collezione chefs
        addToChefRecipes(savedRecipe, recipeId);

        // 3_ Rimuovere la ricetta da quelle in attesa di approvazione nell'admin
        adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId);

        // 4_ Inserire l'evento "inserimento ricetta in Neo4j" nella coda degli eventi che verranno gestiti quando
        // l'utilizzazione della CPU è sotto il 30%
        GraphRecipeDTO graphRecipe = recipeConvertions.MongoToNeo4jGraph(savedRecipe);
        lowLoadManager.addTask(Task.TaskType.CREATE_RECIPE_NEO4J, graphRecipe);

    }


    private void addToChefRecipes(RecipeMongo recipe, String pendingRecipeId) {

        // Controllo esistenza chef
        String chefId = recipe.getChef().getId();

        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

      //if it is a pending recipe
        if (chef.getRecipesToConfirm() != null) {
            chef.getRecipesToConfirm().removeIf(pending -> pending.getId().equals(pendingRecipeId));
        }
        ChefRecipeSummary newChefRecipe = recipeConvertions.recipeToChefRecipe(recipe);

        if(chef.getNewRecipes() == null) {
            chef.setNewRecipes(new java.util.ArrayList<>());
        }

        chef.getNewRecipes().add(0, newChefRecipe);

        if( chef.getNewRecipes().size() > 15 ) {//se consideriamo 3 pagine sono 15 ricette
            ChefRecipeSummary oldestRecipe = chef.getNewRecipes().remove(14);
            OldRecipe oldRecipe = new OldRecipe(oldestRecipe.getId(), oldestRecipe.getNumSaves());//tolgo ultimo elemento
            chef.getOldRecipes().add(0, oldRecipe); //aggiungo alla lista delle ricette vecchie
        }

        //aggiorno
        chef.setTotalRecipes(chef.getTotalRecipes() + 1);
        chefRepository.save(chef);
    }



    /*------------------- Discard a pending recipe  --------------------*/

    public void discardRecipe(String recipeId) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Prendiamo la lista delle ricette in attesa di approvazione
        List<PendingRecipe> recipesToApprove = admin.getRecipesToApprove();

        if (recipesToApprove == null) {
            throw new RuntimeException("No recipe has to be approved");
        }

        String chefId = null;
        for (PendingRecipe recipe : recipesToApprove) {
            if (recipe.getId().equals(recipeId)) {
                chefId = recipe.getChef().getId();
                break;
            }
        }

        // Delete the indicated recipe from the chef list of recipes waiting to be confirmed
        if (chefId == null) {
            throw new RuntimeException("Recipe not found among the ones that have to be approved");
        }
        //ho invertito qui
        boolean recipeFoundAdmin = adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId) > 0;

        if(recipeFoundAdmin) {
            ObjectId chefObjectId = new ObjectId(chefId);
            chefRepository.removeRecipeFromWaiting(chefObjectId, recipeId);
        }
    }


    /*------------------- Approve a pending chef registration request  --------------------*/

    public void approveChef(String chefUsername) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<PendingChef> chefToApprove = admin.getChefsToApprove();
        if (chefToApprove == null) {
            throw new RuntimeException("No chef has to be approved");
        }

        PendingChef chef = null;
        for (PendingChef approvedChef : chefToApprove) {
            if (approvedChef.getUsername().equals(chefUsername)) {
                chef = approvedChef;
                break;
            }
        }

        if (chef == null) {
            throw new RuntimeException("Chef to approve not found");
        }

        Chef chefMongo = chefUtilityFunctions.pendingChefToChef(chef);
        // Salvataggio del nuovo chef nella collection "chefs"

        Chef chefApproved = chefRepository.save(chefMongo);

        // Rimozione dello chef dalla lista di quelli in attesa di approvazione
        adminRepository.removeChefFromApprovals(admin.getId(), chefUsername);

        ChefNeo4j chefNeo4j = new ChefNeo4j();
        chefNeo4j.setMongoId(chefApproved.getId());
        chefNeo4j.setName(chef.getName());
        chefNeo4j.setSurname(chef.getSurname());
        chefNeo4jRepository.save(chefNeo4j);
    }


    /*------------------- Discard a pending chef registration request  --------------------*/

    public void declineChef(String chefUsername) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<PendingChef> chefToApprove = admin.getChefsToApprove();
        if (chefToApprove == null) {
            throw new RuntimeException("No chef has to be approved");
        }

        PendingChef chef = null;
        for (PendingChef newChef : chefToApprove) {
            if (newChef.getUsername().equals(chefUsername)) {
                chef = newChef;
                break;
            }
        }

        if (chef == null) {
            throw new RuntimeException("Chef to approve not found");
        }

        adminRepository.removeChefFromApprovals(admin.getId(), chefUsername);
    }


    /* counting of the monthly foodies */
    public List<YearAnalyticsDTO> getMonthlyFoodies() {
        return foodieRepository.getMonthlyFoodiesStats();
    }

    /*------------------- Emerging vs Declining Categories (Analytics) --------------------*/

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

    public List<PopularIngredientsDTO> getPopularIngredients() {
        return chefNeo4jRepository.getPopularIngredientsStats(COMMON_INGREDIENTS);
    }
/*
    public List<UsedIngredientsDTO> getLeastUsedIngredients() {
        return recipeNeo4jRepository.getCommonIngredients(RARE_INGREDIENTS);
    } */
}


