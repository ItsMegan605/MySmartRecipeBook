package it.unipi.MySmartRecipeBook.utils.populateDB;

import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Order(3)
@Component
public class Neo4jPopulator implements CommandLineRunner {

    @Value("${app.recipe.do-neo4j-population:false}")
    private boolean doNeo4jPopulation;

    private final RecipeMongoRepository recipeRepository;
    public final Neo4jRepository neo4jRepository;
    public final RecipeUtilityFunctions recipeUtils;

    public Neo4jPopulator(RecipeMongoRepository recipeRepository, Neo4jRepository neo4jRepository,
                          RecipeUtilityFunctions recipeUtils) {
        this.recipeRepository = recipeRepository;
        this.neo4jRepository = neo4jRepository;
        this.recipeUtils = recipeUtils;
    }

    @Override
    public void run(String... args) throws Exception {

        if(!doNeo4jPopulation){
            return;
        }

        List<RecipeMongo> listRecipes = recipeRepository.findAll();

        for(RecipeMongo recipe : listRecipes){


        }
    }
}
