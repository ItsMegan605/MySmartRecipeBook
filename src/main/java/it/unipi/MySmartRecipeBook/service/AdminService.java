package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.AnalyticsDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.GraphRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Admin;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.*;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.utils.RecipeUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.enums.Task;
import it.unipi.MySmartRecipeBook.repository.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;

import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final RecipeUtilityFunctions recipeConvertions;
    private final ChefRepository chefRepository;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeRepository;
    private final LowLoadManager lowLoadManager;
    private final FoodieRepository foodieRepository;

    public AdminService(RecipeUtilityFunctions recipeConvertions, ChefRepository chefRepository,
                        AdminRepository adminRepository, RecipeMongoRepository recipeRepository,
                        LowLoadManager lowLoadManager, FoodieRepository foodieRepository) {
        this.recipeConvertions = recipeConvertions;
        this.chefRepository = chefRepository;
        this.adminRepository = adminRepository;
        this.recipeRepository = recipeRepository;
        this.lowLoadManager = lowLoadManager;
        this.foodieRepository = foodieRepository;
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
        List<BaseRecipe> recipesToApprove = admin.getRecipesToApprove();

        if(recipesToApprove == null){
            throw new RuntimeException("No recipe has to be approved");
        }

        BaseRecipe recipeApproved = null;
        for(BaseRecipe recipe : recipesToApprove){
            if(recipe.getId().equals(recipeId)){
                recipeApproved = recipe;
                break;
            }
        }

        if(recipeApproved == null){
            throw new RuntimeException("Recipe not found among the ones that have to be approved");
        }


        // Non vogliamo inserire una nuova ricetta che ha lo stesso titolo di un'altra
        if(recipeRepository.existsByTitle(recipeApproved.getTitle())){
            throw new RuntimeException("Recipe already exists");
        }

        // Quando l'admin approva una ricetta dobbiamo:

        // 1_ Inserire la ricetta in Mongo così da avere anche l'id da inserire nella collezione dello chef
        RecipeMongo recipe = recipeConvertions.baseToMongoRecipe(recipeApproved);
        RecipeMongo savedRecipe = recipeRepository.save(recipe);

        // 2_ Inserire la ricetta tra l'elenco di quelle scritte dallo chef, nalla collezione chefs
        addToChefRecipes(savedRecipe, recipeId);

        // 3_ Rimuovere la ricetta da quelle in attesa di approvazione nell'admin
        adminRepository.removeRecipeFromApprovals(admin.getId(), recipeApproved.getId());

        // 4_ Inserire l'evento "inserimento ricetta in Neo4j" nella coda degli eventi che verranno gestiti quando
        // l'utilizzazione della CPU è sotto il 30%
        GraphRecipeDTO graphRecipe = recipeConvertions.MongoToNeo4jGraph(savedRecipe);
        lowLoadManager.addTask(Task.TaskType.CREATE_RECIPE_NEO4J, graphRecipe);

    }


    private void addToChefRecipes(RecipeMongo recipe, String oldRecipeId) {

        // Controllo esistenza chef
        String chefId = recipe.getChef().getId();

        if(!chefRepository.existsById(chefId)){
            throw new RuntimeException("Chef not found");
        }

        ChefRecipe chefRecipe = recipeConvertions.recipeToChefRecipe(recipe);

        // Viene eliminata la ricetta da quelle in attesa di conferma, viene incrementato il numero totale di ricette
        // dello chef, la ricetta viene inserita nel campo newRecipes (eventualmente rimuovendo una ricetta se l'array
        // ha già dimensione 5)
        chefRepository.approveRecipe(chefId, oldRecipeId, chefRecipe);

    }



    /*------------------- Discard a pending recipe  --------------------*/

    public void discardRecipe(String recipeId) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Prendiamo la lista delle ricette in attesa di approvazione
        List<BaseRecipe> recipesToApprove = admin.getRecipesToApprove();

        if(recipesToApprove == null){
            throw new RuntimeException("No recipe has to be approved");
        }

        String chefId = null;
        for(BaseRecipe recipe : recipesToApprove){
            if(recipe.getId().equals(recipeId)){
                chefId = recipe.getChef().getId();
                break;
            }
        }

        // Delete the indicated recipe from the chef list of recipes waiting to be confirmed
        if(chefId == null){
            throw new RuntimeException("Recipe not found among the ones that have to be approved");
        }

        // Rimuove la ricetta da quelle in attesa di essere confermate nella collezione "chefs"
        chefRepository.removeRecipeFromWaiting(chefId, recipeId);

        // Rimuove la ricetta da quelle da approvare dell'admin
        adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId);

    }


    /*------------------- Approve a pending chef registration request  --------------------*/

    public void approveChef(String chefId) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<Chef> chefToApprove = admin.getChefsToApprove();
        if(chefToApprove == null){
            throw new RuntimeException("No chef has to be approved");
        }

        Chef chef = null;
        for (Chef approvedChef :  chefToApprove) {
            if(approvedChef.getId().equals(chefId)) {
                chef = approvedChef;
                break;
            }
        }

        if (chef == null) {
            throw new RuntimeException("Chef to approve not found");
        }

        // Salvataggio del nuovo chef nella collection "chefs"
        chefRepository.save(chef);

        // Rimozione dello chef dalla lista di quelli in attesa di approvazione
        adminRepository.removeChefFromApprovals(admin.getId(), chefId);
    }


    /*------------------- Discard a pending chef registration request  --------------------*/

    public void declineChef (String chefId) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<Chef> chefToApprove = admin.getChefsToApprove();
        if(chefToApprove == null){
            throw new RuntimeException("No chef has to be approved");
        }

        Chef chef = null;
        for ( Chef newChef :  chefToApprove ) {
            if( newChef.getId().equals(chefId)) {
                chef = newChef;
                break;
            }
        }

        if (chef == null) {
            throw new RuntimeException("Chef to approve not found");
        }

        adminRepository.removeChefFromApprovals(admin.getId(), chefId);
    }


    /* counting of the monthly foodies */
    public List<AnalyticsDTO> getMonthlyFoodies () {
        return foodieRepository.getMonthlyFoodiesStats();
    }



}


