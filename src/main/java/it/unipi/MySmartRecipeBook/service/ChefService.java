package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.RecipeDTO;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Neo4j.RecipeNeo4j;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeNeo4jRepository;
import org.springframework.stereotype.Service;


import it.unipi.MySmartRecipeBook.dto.CreateChefDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateChefDTO;


import java.util.Date;
import java.util.Optional;

@Service
public class ChefService {

    private final ChefRepository chefRepository;

    public ChefService(ChefRepository chefRepository) {
        this.chefRepository = chefRepository;
    }

    /* =========================
       PROFILE MANAGEMENT
       ========================= */

    public Chef createChef(CreateChefDTO dto) {

        Chef chef = new Chef();
        chef.setName(dto.getName());
        chef.setSurname(dto.getSurname());
        chef.setUsername(dto.getUsername());
        chef.setEmail(dto.getEmail());
        chef.setPassword(dto.getPassword());
        chef.setBirthdate(dto.getBirthdate());
        chef.setRegisteredDate(new Date());

        return chefRepository.save(chef);
    }

    public Chef updateChef(String username, UpdateChefDTO dto) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        chef.setName(dto.getName());
        chef.setSurname(dto.getSurname());
        chef.setEmail(dto.getEmail());
        chef.setPassword(dto.getPassword());
        chef.setBirthdate(dto.getBirthdate());

        return chefRepository.save(chef);
    }

    public void deleteChef(String username) {
        chefRepository.findByUsername(username)
                .ifPresent(chefRepository::delete);
    }

    public Optional<Chef> getChefByUsername(String username) {
        return chefRepository.findByUsername(username);
    }
}

/*
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

 */