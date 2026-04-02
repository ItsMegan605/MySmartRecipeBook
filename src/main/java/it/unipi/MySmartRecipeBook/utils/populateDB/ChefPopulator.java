package it.unipi.MySmartRecipeBook.utils.populateDB;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.OldRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.convertionFunctions.ChefUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Order(3)
@Component
public class ChefPopulator implements CommandLineRunner {

    @Value("${app.recipe.do-chef-recipes:false}")
    private boolean doChefRecipes;

    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeChef;

    private final ChefRepository chefRepository;
    private final RecipeMongoRepository recipeRepository;
    private final ChefUtilityFunctions chefUtils;

    public ChefPopulator(ChefRepository chefRepository, RecipeMongoRepository recipeRepository,
                         ChefUtilityFunctions chefUtils) {
        this.chefRepository = chefRepository;
        this.recipeRepository = recipeRepository;
        this.chefUtils = chefUtils;
    }

    @Override
    public void run(String... args) {
        if (!doChefRecipes) return;

        System.out.println("Starting Chef population");
        List<Chef> chefs = chefRepository.findAll();

        for (Chef chef : chefs) {
            if (chef.getUsername().equals("admin")){
                continue;
            }

            List<RecipeMongo> chefRecipes =  recipeRepository.findByChef_IdOrderByCreationDateDesc(chef.getId());

            int limitNew = Math.min(chefRecipes.size(), pageSizeChef * 3);
            List<ChefRecipeSummary> newRecipes = chefUtils.MongoListToChefListSummary(
                    chefRecipes.subList(0, limitNew)
            );

            chef.setNewRecipes(newRecipes);

            int totalSaves = 0;
            for(ChefRecipeSummary recipe: newRecipes){
                if(recipe.getNumSaves() != null){
                    totalSaves += recipe.getNumSaves();
                }
            }

            List<OldRecipe> oldRecipes = new ArrayList<>();
            List<RecipeMongo> oldRecipesMongo = chefRecipes.subList(limitNew, chefRecipes.size());
            for(RecipeMongo recipeMongo : oldRecipesMongo){
               OldRecipe oldRecipe = new OldRecipe(recipeMongo.getId(), recipeMongo.getNumSaves());
               oldRecipes.add(oldRecipe);
               if(oldRecipe.getNumSaves() != null){
                    totalSaves += oldRecipe.getNumSaves();
               }
            }

            chef.setOldRecipes(oldRecipes);
            chef.setTotalSaves(totalSaves);

            List<ChefRecipeSummary> popularRecipes = new ArrayList<>();
            for(RecipeMongo recipe: chefRecipes){
                if(recipe.getNumSaves() != null && recipe.getNumSaves() > 40){
                    popularRecipes.add(chefUtils.recipeToChefRecipe(recipe));
                }
            }

            popularRecipes.sort(Comparator.comparing(ChefRecipeSummary::getNumSaves).reversed());
            chef.setPopularRecipes(popularRecipes);


            int totalRecipes = chefRecipes.size();
            chef.setTotalRecipes(totalRecipes);
            chefRepository.save(chef);

            System.out.println("Finished Chef " + chef.getUsername() + " population");
        }

        System.out.println("Finished Chefs population");
    }
}