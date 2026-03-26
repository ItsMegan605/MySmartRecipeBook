package it.unipi.MySmartRecipeBook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.convertionFunctions.RecipeUtilityFunctions;
import org.springframework.beans.factory.annotation.Value;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unipi.MySmartRecipeBook.utils.parameters.Categories.CATEGORIES;

@Service
public class RecipeService {

    @Value("${app.recipe.pag-size-title:5}")
    private int pageSizeTitle;

    @Value("${app.recipe.pag-size-category:10}")
    private int pageSizeCategory;

    @Value("${app.recipe.pag-size-home:10}")
    private int pageSizeHome;

    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeChef;


    private final RecipeMongoRepository recipeRepository;
    private final RecipeUtilityFunctions convertions;
    private JedisCluster jedisCluster;
    private final ObjectMapper objectMapper;
    private final IngredientService ingredientService;

    public RecipeService(RecipeMongoRepository recipeRepository, RecipeUtilityFunctions convertions,  JedisCluster jedisCluster, ObjectMapper objectMapper, IngredientService ingredientService) {
        this.recipeRepository = recipeRepository;
        this.convertions = convertions;
        this.jedisCluster = jedisCluster;
        this.objectMapper = objectMapper;
        this.ingredientService = ingredientService;
    }

    public boolean isValidIngredient(String ingredientName) {
        return ingredientService.isValidIngredient(ingredientName);
    } //alla fine ogni service ha il suo controllo

    public ShowRecipeDTO getRecipeById(String id, boolean fridge){

        Optional<RecipeMongo> full_recipe = recipeRepository.findById(id);
        if(full_recipe.isEmpty()){
            //remove from redis cache the sinle recipe
            if(fridge){
                try {
                    //recuper utente e il suo frigo
                    UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                            .getAuthentication()
                            .getPrincipal();
                    String fridgeKey = "MySmartRecipeBook:smartFridge:suggestions:" + authFoodie.getUsername();
                    String suggestedRecipes = jedisCluster.get(fridgeKey); //funzione del cluster di jedis per avere il json

                    if( suggestedRecipes != null) { //converto da json in oggetto java con object mapper
                        List<RecipeSuggestionDTO> cachedRecipes = objectMapper.readValue(suggestedRecipes, new TypeReference<List<RecipeSuggestionDTO>>(){});
                        Boolean removedRecipe = cachedRecipes.removeIf(recipe -> recipe.getId().equals(id));

                        if(removedRecipe) {
                            if(cachedRecipes.isEmpty()){
                                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
                            } else  {
                                jedisCluster.set(fridgeKey, objectMapper.writeValueAsString(cachedRecipes));
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }

        return convertions.EntityToDto(full_recipe.get());
    }

    // Si può veramente fare?
    /*public void deleteRecipe(String recipeId) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        }
        recipeRepository.deleteById(recipeId);
        /* Manca l'eliminazione da Neo4j e bisogna vedere se anche da Redis
    }*/

    public SliceRecipeDTO getRecipeByTitle(String title, int pageNumber){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page number");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeTitle);
        Slice<RecipeMongo> matching_recipes = recipeRepository.findByTitleContainingIgnoreCase(title, pageable);
        if (matching_recipes.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(matching_recipes.getContent());
        boolean hasNext = matching_recipes.hasNext();
        boolean hasPrevious = matching_recipes.hasPrevious();

        return  new SliceRecipeDTO<>(recipesDTO, hasNext, hasPrevious);
    }

    public SliceRecipeDTO getNewestRecipe (int pageNumber){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page number");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeHome, Sort.by("creationDate").descending());
        Slice <RecipeMongo> pageResult = recipeRepository.findAll(pageable);

        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(pageResult.getContent());
        boolean hasNext = pageResult.hasNext();
        boolean hasPrevious = pageResult.hasPrevious();
        return new SliceRecipeDTO(recipesDTO, hasNext, hasPrevious);
    }

    public SliceRecipeDTO getByCategory (int pageNumber, String filter){

        if(pageNumber <= 0 || !CATEGORIES.contains(filter)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeCategory, Sort.by("creationDate").descending());
        Slice<RecipeMongo> matching_list = recipeRepository.findByCategory(filter, pageable);

        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(matching_list.getContent());
        boolean hasNext = matching_list.hasNext();
        boolean hasPrevious = matching_list.hasPrevious();

        return new SliceRecipeDTO(recipesDTO, hasNext, hasPrevious);
    }


    public SliceRecipeDTO getChefRecipePage(int pageNumber, String chefId){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeChef, Sort.by("totalSaves").descending());
        Slice<RecipeMongo> matching_recipes = recipeRepository.findByChef_Id(chefId, pageable);

        if (matching_recipes.isEmpty()){
            return new SliceRecipeDTO(List.of(), false, false);
        }

        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(matching_recipes.getContent());
        boolean hasNext = matching_recipes.hasNext();
        boolean hasPrevious = matching_recipes.hasPrevious();

        return new SliceRecipeDTO(recipesDTO, hasNext, hasPrevious);
    }
}

