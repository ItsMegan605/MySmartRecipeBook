package it.unipi.MySmartRecipeBook.utils.populateDB;

import it.unipi.MySmartRecipeBook.model.Ingredient;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.IngredientRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.utils.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Order(3)
@Component
public class Neo4jPopulator implements CommandLineRunner {

    @Value("${app.recipe.do-neo4j-population:false}")
    private boolean doNeo4jPopulation;

    private final RecipeMongoRepository recipeRepository;
    public final RecipeNeo4jRepository neo4jRepository;
    public final RecipeUtilityFunctions recipeUtils;
    public final IngredientRepository ingredientRepository;

    public Neo4jPopulator(RecipeMongoRepository recipeRepository, RecipeNeo4jRepository neo4jRepository,
                          RecipeUtilityFunctions recipeUtils, IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.neo4jRepository = neo4jRepository;
        this.recipeUtils = recipeUtils;
        this.ingredientRepository = ingredientRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if(!doNeo4jPopulation){
            return;
        }

        // Pulisco tutto prima di farlo ripartire
        neo4jRepository.deleteAll();

        System.out.println("Starting Neo4j population");
        List<Ingredient> ingredients = ingredientRepository.findAll();
        for(Ingredient ingredient : ingredients){
            neo4jRepository.insertIngredient(ingredient.getId(), ingredient.getName());
        }

        List<RecipeMongo> listRecipes = recipeRepository.findAll();

        for(RecipeMongo recipe : listRecipes){
            List<String> ingredientsName = new ArrayList<>();
            for(Ingredient ingredient : recipe.getIngredients()){
                ingredientsName.add(ingredient.getName());
            }
            neo4jRepository.createRecipe(recipe.getId(), recipe.getTitle(), recipe.getChef().getId(), ingredientsName);
            System.out.println("Recipe " + recipe.getId() + " has been created");
        }
        System.out.println("Finished Neo4j population");
    }
}