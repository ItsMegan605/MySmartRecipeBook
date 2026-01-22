package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.RecipeDTO;
import it.unipi.MySmartRecipeBook.model.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChefService {

    private final RecipeMongoRepository mongoRepo;
    private final RecipeNeo4jRepository neo4jRepo;

    public ChefService(RecipeMongoRepository mongoRepo, RecipeNeo4jRepository neo4jRepo) {
        this.mongoRepo = mongoRepo;
        this.neo4jRepo = neo4jRepo;
    }

    // EXTRA: @Transactional qui gestisce solo Neo4j o JPA.
    // Mongo ha transazioni separate. In un sistema reale useresti il pattern SAGA se uno fallisce.
    public void createRecipe(RecipeDTO dto) {
        // 1. Salva i dettagli completi su MongoDB
        RecipeMongo mongoRecipe = new RecipeMongo();
        mongoRecipe.setTitle(dto.getTitle());
        mongoRecipe.setDescription(dto.getDescription());
        mongoRecipe.setIngredients(dto.getIngredients()); // Lista completa stringhe
        // ... setta altri campi ...
        RecipeMongo savedMongo = mongoRepo.save(mongoRecipe);


        // 2. Salva la struttura del grafo su Neo4j
        // Usiamo lo stesso ID di Mongo per collegarli logicamente!
        RecipeNeo4j neoRecipe = new RecipeNeo4j();
        neoRecipe.setId(savedMongo.getId());
        neoRecipe.setTitle(dto.getTitle());
        // EXTRA: Qui dovresti avere logica per creare nodi "Ingredient" e collegarli
        neo4jRepo.save(neoRecipe);
    }
}