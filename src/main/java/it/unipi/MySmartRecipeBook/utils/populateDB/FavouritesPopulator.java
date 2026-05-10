package it.unipi.MySmartRecipeBook.utils.populateDB;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.FoodieUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Populator for the Foodie collection in MongoDB with random favorite recipes.
 */
@Order(2)
@Component
public class FavouritesPopulator implements CommandLineRunner {

    private final RecipeUtilityFunctions recipeUtilityFunctions;
    @Value("${app.recipe.do-population:false}")
    private boolean doPopulation;

    private final RecipeMongoRepository recipeRepository;
    private final FoodieRepository foodieRepository;
    private final FoodieUtilityFunctions foodieUtils;


    public FavouritesPopulator(RecipeMongoRepository recipeRepository, FoodieRepository foodieRepository,
                               FoodieUtilityFunctions foodieUtils, RecipeUtilityFunctions recipeUtilityFunctions) {
        this.recipeRepository = recipeRepository;
        this.foodieRepository = foodieRepository;
        this.foodieUtils = foodieUtils;
        this.recipeUtilityFunctions = recipeUtilityFunctions;
    }

    /**
     * Executes the foodie favorites population script on application startup if enabled in
     * application properties
     * Assigns a random number of saved recipes (up to 200) to each foodie.
     * @param args command line arguments
     */
    @Override
    public void run(String... args){

        if(!doPopulation){
            return;
        }

        System.out.println("Populating mongo DB");

        List<RecipeMongo> recipes = recipeRepository.findAll();
        List<Foodie> foodies = foodieRepository.findAll();

        if(recipes.isEmpty() || foodies.isEmpty()){
            return;
        }

        int bound = recipes.size() < 200 ?  recipes.size() : 200;
        Random random = new Random();
        for(Foodie foodie : foodies){
            //choose a random number of recipes to save as favourites
            int numRecipes = random.nextInt(bound);

            int addedRecipes = 0;

            Set<Integer> chosenIndices = new HashSet<>();
            List<FoodieRecipeSummary> foodieRecipes = new ArrayList<>();
            List<String> recipesId = new ArrayList<>();

            while(addedRecipes < numRecipes){

                int randomIndex = random.nextInt(recipes.size());

                if(chosenIndices.add(randomIndex)) {

                    RecipeMongo recipe = recipes.get(randomIndex);
                    FoodieRecipeSummary fullRecipe = recipeUtilityFunctions.entityToReducedRecipe(recipe);
                    foodieRecipes.add(fullRecipe);
                    recipesId.add(recipe.getId());

                    /* Update the total number of saves in the recipes collection */
                    recipeRepository.updateSavesCounter(recipe.getId(), 1);

                    addedRecipes++;
                }

            }
            foodieRepository.addRecipesToFavourites(foodie.getId(), recipesId, foodieRecipes);
            System.out.println("Foodie '" + foodie.getUsername() + "' population completed");
        }

        System.out.println("Population completed");
    }
}
