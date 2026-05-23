package it.unipi.MySmartRecipeBook.utils.populateDB;

import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import it.unipi.MySmartRecipeBook.model.Neo4j.IngredientNeo4j;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.ChefNeo4jRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.IngredientNeo4jRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Command line runner that populates the Neo4j graph database.
 * It synchronizes Chefs, Recipes, Ingredients, and their relationships from MongoDB.
 */
@Order(4) //order to execute the functions to populate the DBs
@Component
public class Neo4jPopulator implements CommandLineRunner {


    @Value("${app.recipe.do-neo4j-population:false}")
    private boolean doNeo4jPopulation;

    private final RecipeMongoRepository recipeRepository;
    public final RecipeNeo4jRepository neo4jRepository;
    public final RecipeUtilityFunctions recipeUtils;
    public final ChefNeo4jRepository chefNeo4jRepository;
    private final ChefRepository chefRepository;
    private final IngredientNeo4jRepository ingredientNeo4jRepository;
    private final JedisSentinelPool jedisSentinelPool;
    private final Neo4jClient neo4jClient;


    public Neo4jPopulator(RecipeMongoRepository recipeRepository, RecipeNeo4jRepository neo4jRepository,
                          RecipeUtilityFunctions recipeUtils, ChefNeo4jRepository chefNeo4jRepository,
                          ChefRepository chefRepository, IngredientNeo4jRepository ingredientNeo4jRepository,
                          JedisSentinelPool jedisSentinelPool, Neo4jClient neo4jClient) {
        this.recipeRepository = recipeRepository;
        this.neo4jRepository = neo4jRepository;
        this.recipeUtils = recipeUtils;
        this.chefNeo4jRepository = chefNeo4jRepository;
        this.chefRepository = chefRepository;
        this.ingredientNeo4jRepository = ingredientNeo4jRepository;
        this.jedisSentinelPool = jedisSentinelPool;
        this.neo4jClient = neo4jClient;
    }

    /**
     * Executes the Neo4j graph population script on application startup if enabled.
     * @param args command line arguments
     * @throws Exception if a database operation fails
     */
    @Override
    public void run(String... args) throws Exception {

        if(!doNeo4jPopulation){
            return;
        }

        //clear the graph
        System.out.println("Cleaning Neo4j");
        neo4jRepository.deleteAll();
        chefNeo4jRepository.deleteAll();
        ingredientNeo4jRepository.deleteAll();

        System.out.println("Starting Neo4j population");
        //create ingredient node on neo4j
        System.out.println("Creating ingredient nodes");

        Set<String> ingredients;
        try (Jedis jedis = jedisSentinelPool.getResource()) {
            ingredients = jedis.smembers("MySmartRecipeBook:allowed_ingredients");
        }
        List<IngredientNeo4j> ingredientsNeo4j = new ArrayList<>();
        for(String ingredient : ingredients){
            IngredientNeo4j ingredientNeo4j = new IngredientNeo4j();
            ingredientNeo4j.setName(ingredient);
            ingredientsNeo4j.add(ingredientNeo4j);
        }
        ingredientNeo4jRepository.saveAll(ingredientsNeo4j);

        //create chef node excluding admin
        System.out.println("Creating chef nodes");
        List<ChefNeo4j> chefsNeo4j = new ArrayList<>();
        List<Chef> chefs = chefRepository.findAll();

        for(Chef chef : chefs){
            if(chef.getUsername().equals("admin")) {
                continue;
            }

            ChefNeo4j newChef = new ChefNeo4j();
            newChef.setMongoId(chef.getId());
            newChef.setName(chef.getName());
            newChef.setSurname(chef.getSurname());
            chefsNeo4j.add(newChef);
        }
        chefNeo4jRepository.saveAll(chefsNeo4j);

        neo4jClient.query("CREATE INDEX temp_chef_id IF NOT EXISTS FOR (c:Chef) ON (c.mongo_id)").run();
        neo4jClient.query("CREATE INDEX ingredient_name IF NOT EXISTS FOR (i:Ingredient) ON (i.name)").run();

        neo4jClient.query("CALL db.awaitIndexes()").run();
        // get the recipe list and create the recipe node
        System.out.println("Creating recipe nodes");

        List<RecipeMongo> listRecipes = recipeRepository.findAll();
        for(RecipeMongo recipe : listRecipes){

            List<String> recipeIngredients = new ArrayList<>();
            for(RecipeIngredient ingredient : recipe.getIngredients()) {
                String ingredientName = ingredient.getName().toLowerCase().trim();
                recipeIngredients.add(ingredientName);
            }

            neo4jRepository.createRecipe(recipe.getId(), recipe.getTitle(), recipe.getImageURL(), recipe.getCategory(),
                    recipe.getChef().getId(), recipeIngredients);

        }

        neo4jClient.query("DROP INDEX temp_chef_id IF EXISTS").run();
        System.out.println("Finished Neo4j population");
    }
}