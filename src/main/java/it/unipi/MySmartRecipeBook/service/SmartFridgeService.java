package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.model.SmartFridge;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.model.SmartFridgeIngredient;
import it.unipi.MySmartRecipeBook.repository.SmartFridgeRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SmartFridgeService {

    private final SmartFridgeRepository fridgeRepo; // Redis
    private final RecipeNeo4jRepository neo4jRepo;  // Graph Search
    private final RecipeMongoRepository mongoRepo;  // Details

    public SmartFridgeService(SmartFridgeRepository fridgeRepo, RecipeNeo4jRepository neo4jRepo, RecipeMongoRepository mongoRepo) {
        this.fridgeRepo = fridgeRepo;
        this.neo4jRepo = neo4jRepo;
        this.mongoRepo = mongoRepo;
    }

    public void addIngredientToFridge(Integer userId, String ingredient) {
        // Usa Integer per coerenza con il modello SmartFridge
        SmartFridge fridge = fridgeRepo.findById(userId)
                .orElse(new SmartFridge(userId));

        // Usa il metodo addProduct definito nel tuo modello SmartFridge
        fridge.addProduct(null, ingredient); // ingredientId può essere null se non lo gestisci
        fridgeRepo.save(fridge);
    }

    public List<RecipeMongo> whatCanICook(Integer userId) {
        SmartFridge fridge = fridgeRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Frigo vuoto!"));

        // Estrai i nomi (String) dalla lista di SmartFridgeIngredient
        List<String> myIngredients = fridge.getProducts().stream()
                .map(SmartFridgeIngredient::getIngredientName)
                .collect(Collectors.toList());

        // Chiamata a Neo4j (che ora filtra per >= 3 match)
        List<RecipeNeo4j> possibleMatches = neo4jRepo.findRecipesByIngredients(myIngredients);

        List<String> recipeIds = possibleMatches.stream()
                .map(RecipeNeo4j::getId)
                .collect(Collectors.toList());

        // Recupera i dettagli completi da MongoDB
        return (List<RecipeMongo>) mongoRepo.findAllById(recipeIds);
    }

}