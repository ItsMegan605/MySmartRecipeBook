package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.model.Admin;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.RecipeConvertions;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void saveRecipe(String recipeId) {

        Admin admin = adminRepository.findFirstBy();
        if (admin == null) {
            throw new RuntimeException("Admin not found");
        }
        List<RecipeMongo> recipesToApprove = admin.getRecipesToApprove();

        if(recipesToApprove == null){
            throw new RuntimeException("No recipe has to be approved");
        }

        RecipeMongo recipeApproved = null;
        for(RecipeMongo recipe : recipesToApprove){
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
        recipeRepository.save(recipeApproved);

    }

    private void addToChefRecipes(RecipeMongo recipe) {

        String chefId = recipe.getChef().getMongoId();
        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

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

        ChefRecipe full_recipe = recipeConvertions.entityToChefRecipe(recipe);
        chef.getNewRecipes().add(full_recipe);
        chefRepository.save(chef);
    }

    /*
    Con aggiunta in un secondo momento
    private RecipeNeo4j createRecipeNeo4j(CreateRecipeDTO dto){
*/
    }
