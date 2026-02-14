package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final RecipeMongoRepository recipeMongoRepository;
    private final RecipeNeo4jRepository recipeNeo4jRepository;


    public AdminService(RecipeMongoRepository recipeMongoRepository, RecipeNeo4jRepository recipeNeo4jRepository) {
        this.recipeMongoRepository = recipeMongoRepository;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
    }

    public void deleteRecipe(String recipeId) {
        recipeMongoRepository.deleteById(recipeId);
        recipeNeo4jRepository.deleteById(recipeId);
        System.out.println("Admin deleted recipe with ID: " + recipeId);
        //mettere eventuali controlli e altro quando abbiao sistemato
        //se abbiamo anlytics con i trend delle ricette etc dobbiao anche toglierle da lì
    }
}
