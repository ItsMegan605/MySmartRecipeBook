package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.ReducedChef;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class AdminService {

    public void saveRecipe(String recipeId) {

        RecipeMongo savedRecipe = createRecipeMongo(dto);
        //createRecipeNeo4j(dto);

        addToChefRecipes(savedRecipe);

        ChefPreviewRecipeDTO recipeDTO = convertions.EntityToChefDto(savedRecipe);
        return recipeDTO;

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
            ChefRecipeSummary reduced_old = convertions.entityToReducedRecipe(oldestRecipe);
            chef.getOldRecipes().add(reduced_old);
        }

        ChefRecipe full_recipe = convertions.entityToChefRecipe(recipe);
        chef.getNewRecipes().add(full_recipe);
        chefRepository.save(chef);

    }


    private RecipeMongo createRecipeMongo(CreateRecipeDTO dto){

        RecipeMongo recipe = new RecipeMongo();
        recipe.setTitle(dto.getTitle());
        recipe.setCategory(dto.getCategory());
        recipe.setPreparation(dto.getPreparation());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setPresentation(dto.getPresentation());
        recipe.setImageURL(dto.getImageURL());
        recipe.setIngredients(dto.getIngredients());
        recipe.setCreationDate(LocalDateTime.now());

        ReducedChef chef = new ReducedChef();
        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        chef.setMongoId(chef1.getId());
        chef.setName(chef1.getName());
        chef.setSurname(chef1.getSurname());

        recipe.setChef(chef);
        return recipeRepository.save(recipe);
    }

    /*
    Con aggiunta in un secondo momento
    private RecipeNeo4j createRecipeNeo4j(CreateRecipeDTO dto){

    }*/
}
