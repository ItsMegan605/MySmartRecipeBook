package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.model.Admin;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.AdminRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.RecipeConvertions;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final RecipeConvertions recipeConvertions;
    private final ChefRepository chefRepository;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeRepository;
    public AdminService(RecipeConvertions recipeConvertions, ChefRepository chefRepository,
                        AdminRepository adminRepository, RecipeMongoRepository recipeRepository) {
        this.recipeConvertions = recipeConvertions;
        this.chefRepository = chefRepository;
        this.adminRepository = adminRepository;
        this.recipeRepository = recipeRepository;
    }



    //@Transactional
    public void saveRecipe(String recipeId) {

        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<AdminRecipe> recipesToApprove = admin.getRecipesToApprove();

        if(recipesToApprove == null){
            throw new RuntimeException("No recipe has to be approved");
        }

        AdminRecipe recipeApproved = null;
        for(AdminRecipe recipe : recipesToApprove){
            if(recipe.getId().equals(recipeId)){
                recipeApproved = recipe;
                recipesToApprove.remove(recipeApproved);
                break;
            }
        }

        if(recipeApproved == null){
            throw new RuntimeException("Recipe not found");
        }

        addToChefRecipes(recipeApproved);
        adminRepository.save(admin);

        RecipeMongo recipe = recipeConvertions.adminToMongoRecipe(recipeApproved);
        recipeRepository.save(recipe);

    }

    private void addToChefRecipes(AdminRecipe recipe) {

        String chefId = recipe.getChef().getMongoId();
        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));


        /* In questo caso non c'è nessun controllo sul fatto che la ricetta effettivamente fosse presente tra quelle da confermare*/
        List<ChefRecipe> recipesToConfirm = chef.getRecipesToConfirm();

        for (ChefRecipe singleRecipe : recipesToConfirm){
            if(singleRecipe.getId().equals(recipe.getId())){
                recipesToConfirm.remove(singleRecipe);
                break;
            }
        }

        if(chef.getNewRecipes() == null){
            chef.setNewRecipes(new ArrayList<>());
        }
        else if (chef.getNewRecipes().size() == 5) {

            if(chef.getOldRecipes() == null){
                chef.setOldRecipes(new ArrayList<>());
            }

            ChefRecipe oldestRecipe = chef.getNewRecipes().remove(0);
            ChefRecipeSummary reduced_old = recipeConvertions.entityToReducedRecipe(oldestRecipe);
            chef.getOldRecipes().add(reduced_old);
        }

        ChefRecipe full_recipe = recipeConvertions.adminToChefRecipe(recipe);
        chef.getNewRecipes().add(full_recipe);
        chefRepository.save(chef);
    }

    /*
    Con aggiunta in un secondo momento
    private RecipeNeo4j createRecipeNeo4j(CreateRecipeDTO dto){
*/
    public void discardRecipe(String recipeId) {

    }
}
