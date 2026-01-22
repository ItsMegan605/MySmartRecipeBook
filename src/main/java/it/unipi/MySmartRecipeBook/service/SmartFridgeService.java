package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.model.SmartFridge;
import it.unipi.MySmartRecipeBook.model.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.RecipeNeo4j;
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

    // 1. Aggiungi ingrediente al frigo (Redis)
    public void addIngredientToFridge(String userId, String ingredient) {
        SmartFridge fridge = fridgeRepo.findById(userId).orElse(new SmartFridge(userId, new ArrayList<>()));
        fridge.getIngredients().add(ingredient);
        fridgeRepo.save(fridge);
    }

    // 2. "Cosa posso cucinare?"
    public List<RecipeMongo> whatCanICook(String userId) {
        // A. Prendi ingredienti da Redis
        SmartFridge fridge = fridgeRepo.findById(userId).orElseThrow(() -> new RuntimeException("Frigo vuoto!"));
        List<String> myIngredients = fridge.getIngredients();

        // B. Chiedi a Neo4j le ricette compatibili (ritorna oggetti leggeri)
        List<RecipeNeo4j> possibleMatches = neo4jRepo.findRecipesByIngredients(myIngredients);

        // C. Estrai gli ID
        List<String> recipeIds = possibleMatches.stream()
                .map(RecipeNeo4j::getId)
                .collect(Collectors.toList());

        // D. Prendi i dettagli completi da Mongo (titolo, foto, preparazione)
        return (List<RecipeMongo>) mongoRepo.findAllById(recipeIds);
    }
}