package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.RecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeService {

    private static final List<String> VALID_FILTERS = List.of(
            "difficulty",
            "prep_time",
            "vegan",
            "dairy-free",
            "gluten-free",
            "main-course",
            "second-course",
            "dessert"
    );

    private static final List<String> VALID_CATEGORIES = List.of(
            "vegan",
            "dairy-free",
            "gluten-free",
            "main-course",
            "second-course",
            "dessert"
    );

    private final RecipeMongoRepository recipeRepository;

    public RecipeService(RecipeMongoRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /* =========================
       CRUD OPERATIONS
       ========================= */

    private RecipeMongo createRecipeMongo(RecipeDTO dto){

        RecipeMongo recipe = new RecipeMongo();
        recipe.setTitle(dto.getTitle());
        recipe.setCategory(dto.getCategory());
        recipe.setPreparation(dto.getPreparation());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setDescription(dto.getDescription());
        recipe.setImageURL(dto.getImageURL());
        // ci va messo lo username dell'utente loggato (chef)
        //recipe.setChefUsername();
        recipe.setIngredients(dto.getIngredients());
        recipe.setCreationDate(LocalDateTime.now());

        return recipeRepository.save(recipe);
    }

    /*
    private RecipeNeo4j createRecipeNeo4j(CreateRecipeDTO dto){

    }*/

    public ChefPreviewRecipeDTO createRecipe(RecipeDTO dto) {

        RecipeMongo savedRecipe = createRecipeMongo(dto);
        //createRecipeNeo4j(dto);

        // bisognerebbe prenderere l'elenco degli chef e aggiungere a quello
        // attualmente loggato l'id di questa ricetta
        ChefPreviewRecipeDTO recipeDTO = convert_entity_chef_dto(savedRecipe);

        return recipeDTO;
    }



    public RecipeDTO getRecipeById(String id){

        RecipeMongo full_recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        RecipeDTO recipeDTO = convert_entity_dto(full_recipe);
        return recipeDTO;
    }

    public void deleteRecipe(String recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }
        recipeRepository.deleteById(recipeId);
    }

    public List<RecipeDTO> getRecipeByTitle(String title){

        List<RecipeMongo> matching_recipes = recipeRepository.findByTitleContainingIgnoreCase(title);
        if (matching_recipes.size() == 0){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        List<RecipeDTO> recipes = new ArrayList<>();
        for (RecipeMongo full_recipe : matching_recipes){

            RecipeDTO recipeDTO = convert_entity_dto(full_recipe);
            recipes.add(recipeDTO);
        }
        return recipes;
    }

    private RecipeDTO convert_entity_dto(RecipeMongo recipe){
        RecipeDTO recipeDTO = new RecipeDTO();
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setDescription(recipe.getDescription());
        recipeDTO.setCategory(recipe.getCategory());
        recipeDTO.setPrepTime(recipe.getPrepTime());
        recipeDTO.setDifficulty(recipe.getDifficulty());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setPreparation(recipe.getPreparation());
        recipeDTO.setIngredients(recipe.getIngredients());
        recipeDTO.setCreationDate(recipe.getCreationDate());

        return recipeDTO;
    }

    private UserPreviewRecipeDTO convert_entity_user_dto(RecipeMongo recipe){
        UserPreviewRecipeDTO recipeDTO = new UserPreviewRecipeDTO();
        recipeDTO.setMongo_id(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setDescription(recipe.getDescription());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setChefUsername(recipe.getChefUsername());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }

    private ChefPreviewRecipeDTO convert_entity_chef_dto(RecipeMongo recipe){
        ChefPreviewRecipeDTO recipeDTO = new ChefPreviewRecipeDTO();
        recipeDTO.setMongo_id(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setDescription(recipe.getDescription());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }

    // Non mi piace per nulla, troppe cose importate
    public List<UserPreviewRecipeDTO> getRecipePage(Integer pageNumber, Integer pageSize){

        if(pageNumber <= 0 || pageSize <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        // Spring traduce questo in:
        // "db.recipes.find().sort({creationDate: -1}).skip(pageNumber * 10).limit(pageSize)"
        Pageable pageable = PageRequest.of(--pageNumber, pageSize, Sort.by("creationDate").descending());
        Page<RecipeMongo> pageResult = recipeRepository.findAll(pageable);

        List<UserPreviewRecipeDTO> recipe_list = new ArrayList<>();
        for (RecipeMongo recipe: pageResult.getContent()){
            UserPreviewRecipeDTO recipeDTO = convert_entity_user_dto(recipe);
            recipe_list.add(recipeDTO);
        }
        return recipe_list;
    }

    // Questa deve essere modificata conoscendo l'utente loggato
    public List<UserPreviewRecipeDTO> getUserRecipePage(Integer pageNumber, Integer pageSize, String filter){

        if(pageNumber <= 0 || pageSize <= 0 || !VALID_FILTERS.contains(filter)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        Page<RecipeMongo> pageResult;
        if(VALID_CATEGORIES.contains(filter)){
            Pageable pageable = PageRequest.of(--pageNumber, pageSize);
            pageResult = recipeRepository.findByCategory(filter, pageable);
        }
        else {
            // Spring traduce questo in:
            // "db.recipes.find().sort({creationDate: -1}).skip(pageNumber * 10).limit(pageSize)"
            Pageable pageable = PageRequest.of(--pageNumber, pageSize, Sort.by(filter).descending());
            pageResult = recipeRepository.findAll(pageable);
        }

        List<UserPreviewRecipeDTO> recipe_list = new ArrayList<>();
        for (RecipeMongo recipe: pageResult.getContent()){
            UserPreviewRecipeDTO recipeDTO = convert_entity_user_dto(recipe);
            recipe_list.add(recipeDTO);
        }
        return recipe_list;
    }

    public List<ChefPreviewRecipeDTO> getChefRecipePage(Integer pageNumber, Integer pageSize, String filter){

        if(pageNumber <= 0 || pageSize <= 0 || !VALID_FILTERS.contains(filter)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        Page<RecipeMongo> pageResult;
        if(VALID_CATEGORIES.contains(filter)){
            Pageable pageable = PageRequest.of(--pageNumber, pageSize);
            pageResult = recipeRepository.findByCategory(filter, pageable);
        }
        else {
            // Spring traduce questo in:
            // "db.recipes.find().sort({creationDate: -1}).skip(pageNumber * 10).limit(pageSize)"
            Pageable pageable = PageRequest.of(--pageNumber, pageSize, Sort.by(filter).descending());
            pageResult = recipeRepository.findAll(pageable);
        }

        List<ChefPreviewRecipeDTO> recipe_list = new ArrayList<>();
        for (RecipeMongo recipe: pageResult.getContent()){
            ChefPreviewRecipeDTO recipeDTO = convert_entity_chef_dto(recipe);
            recipe_list.add(recipeDTO);
        }
        return recipe_list;
    }


}

