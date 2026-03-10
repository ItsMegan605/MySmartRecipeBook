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
import redis.clients.jedis.JedisPooled;

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
    private JedisPooled jedisPool;
    private final ObjectMapper objectMapper;
    public RecipeService(RecipeMongoRepository recipeRepository, RecipeUtilityFunctions convertions,  JedisPooled jedisPool, ObjectMapper objectMapper) {
        this.recipeRepository = recipeRepository;
        this.convertions = convertions;
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }


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
                    String fridgeKey = "smartFridge:suggestions:" + authFoodie.getUsername();
                    String suggestedRecipes = jedisPool.get(fridgeKey); //funzione del cluster di jedis per avere il json

                    if( suggestedRecipes != null) { //converto da json in oggetto java con object mapper
                        List<RecipeSuggestionDTO> cachedRecipes = objectMapper.readValue(suggestedRecipes, new TypeReference<List<RecipeSuggestionDTO>>(){});
                       Boolean removedRecipe = cachedRecipes.removeIf(recipe -> recipe.getId().equals(id));

                       if(removedRecipe) {
                           if(cachedRecipes.isEmpty()){
                               throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
                           } else  {
                               jedisPool.set(fridgeKey, objectMapper.writeValueAsString(cachedRecipes));
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

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSizeCategory);
        Slice<RecipeMongo> matching_list = recipeRepository.findByCategory(filter, pageable);

        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(matching_list.getContent());
        boolean hasNext = matching_list.hasNext();
        boolean hasPrevious = matching_list.hasPrevious();

        return new SliceRecipeDTO(recipesDTO, hasNext, hasPrevious);
    }

    /* Per ora sono stati ordinati per data ma andrebbero ordinate per popolarità*/
    public SliceRecipeDTO getChefRecipePage(int pageNumber, String chefName){

        if(pageNumber <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        }

        Pageable pageable = PageRequest.of(--pageNumber, pageSizeChef, Sort.by("totalSaves").descending());
        Slice<RecipeMongo> matching_recipes = recipeRepository.findByChef_Name(chefName, pageable);

        if (matching_recipes.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        List<UserPreviewRecipeDTO> recipesDTO = convertions.EntityToUserDto(matching_recipes.getContent());
        boolean hasNext = matching_recipes.hasNext();
        boolean hasPrevious = matching_recipes.hasPrevious();

        return new SliceRecipeDTO(recipesDTO, hasNext, hasPrevious);
    }
}

