package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.RecipeDTO;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import org.springframework.stereotype.Service;

@Service
public class ChefService {

    private final RecipeMongoRepository mongoRepo;
    private final RecipeNeo4jRepository neo4jRepo;
    private final ChefRepository chefRepository;

    public ChefService(RecipeMongoRepository mongoRepo,
                       RecipeNeo4jRepository neo4jRepo,
                       ChefRepository chefRepository) {
        this.mongoRepo = mongoRepo;
        this.neo4jRepo = neo4jRepo;
        this.chefRepository = chefRepository;
    }

    public Chef login(String username, String password) {
        // Ritorna lo chef se la coppia username/password esiste nel DB
        return chefRepository.findByUsernameAndPassword(username, password)
                .orElse(null);
    }

    public void createRecipe(RecipeDTO dto) {
        RecipeMongo mongoRecipe = new RecipeMongo();
        mongoRecipe.setTitle(dto.getTitle());
        mongoRecipe.setDescription(dto.getDescription());
        mongoRecipe.setIngredients(dto.getIngredients());
        RecipeMongo savedMongo = mongoRepo.save(mongoRecipe);

        RecipeNeo4j neoRecipe = new RecipeNeo4j();
        neoRecipe.setId(savedMongo.getId());
        neoRecipe.setTitle(dto.getTitle());
        neo4jRepo.save(neoRecipe);
    }
}