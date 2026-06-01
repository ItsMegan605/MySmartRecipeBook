package it.unipi.MySmartRecipeBook.service;

import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.*;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.FoodiePreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.ChefPreviewDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.TopChefDTO;
import it.unipi.MySmartRecipeBook.dto.users.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.ChefNeo4jRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import it.unipi.MySmartRecipeBook.event.Task;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.FoodieUtilityFunctions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Foodie service with business logic
 */
@Service
public class FoodieService {

    private final RecipeUtilityFunctions recipeUtilityFunctions;
    @Value("${app.recipe.pag-size-foodie:5}")
    private int pageSizeFoodie;

    private final FoodieRepository foodieRepository;
    private final RecipeMongoRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FoodieUtilityFunctions usersConvertions;
    private final LowLoadManager lowLoadManager;
    private final MongoTemplate mongoTemplate;
    private final ChefNeo4jRepository chefNeo4jRepository;
    private final ChefUtilityFunctions chefConvertions;
    private final ChefRepository chefRepository;

    public FoodieService(FoodieRepository foodieRepository, RecipeMongoRepository recipeRepository,
                         PasswordEncoder passwordEncoder, FoodieUtilityFunctions usersConvertions,
                         LowLoadManager lowLoadManager, MongoTemplate mongoTemplate, RecipeUtilityFunctions recipeUtilityFunctions,
                         ChefNeo4jRepository chefNeo4jRepository, ChefUtilityFunctions chefConvertions, ChefRepository chefRepository) {
        this.foodieRepository = foodieRepository;
        this.recipeRepository = recipeRepository;
        this.passwordEncoder = passwordEncoder;
        this.usersConvertions = usersConvertions;
        this.lowLoadManager = lowLoadManager;
        this.mongoTemplate = mongoTemplate;
        this.recipeUtilityFunctions = recipeUtilityFunctions;
        this.chefNeo4jRepository = chefNeo4jRepository;
        this.chefConvertions = chefConvertions;
        this.chefRepository = chefRepository;
    }

    /**
     * Retrieve foodie's information
     * @return the foodie's profile
     */

    public RegisteredUserInfoDTO getById() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        return usersConvertions.entityToFoodieDTO(foodie);
    }


    /**
     * This function allows a foodie to change his/her personal information, except the username
     * @param dto - the foodie's dto
     * @return the new changed information
     */
    public RegisteredUserInfoDTO updateFoodie(UpdateFoodieDTO dto) {

        if(dto == null || dto.isEmpty()){
            throw new IllegalArgumentException("Invalid parameters");
        }

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!foodieRepository.existsById(authFoodie.getId())) {
            throw new NoSuchElementException("Foodie not found");
        }

        Query query = new Query(Criteria.where("id").is(authFoodie.getId()));

        Update update = new Update();
        if (dto.getName() != null && StringUtils.hasText(dto.getName()))
            update.set("name", dto.getName());

        if (dto.getSurname() != null && StringUtils.hasText(dto.getSurname()))
            update.set("surname", dto.getSurname());

        if (dto.getEmail() != null && StringUtils.hasText(dto.getEmail()))
            update.set("email", dto.getEmail());

        if (dto.getPassword() != null && StringUtils.hasText(dto.getPassword()))
            update.set("password", passwordEncoder.encode(dto.getPassword()));

        if (dto.getBirthdate() != null)
            update.set("birthdate", dto.getBirthdate());

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        Foodie foodie = mongoTemplate.findAndModify(query, update, options, Foodie.class);

        if (foodie == null) {
            throw new NoSuchElementException("Foodie not found");
        }
        return usersConvertions.entityToFoodieDTO(foodie);
    }


    /**
     * Delete foodie's profile
     */
    @Transactional
    public void deleteFoodie() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        List<String> recipesId = new ArrayList<>();
        Map<String, List<String>> recipesByChefId = new HashMap<>();

        if (foodie.getSavedRecipes() != null) {
            for (FoodieRecipeSummary recipe : foodie.getSavedRecipes()) {
                String recipeId = recipe.getId();
                String chefId = recipe.getChef().getId();

                recipesId.add(recipeId);

                recipesByChefId.computeIfAbsent(chefId, k -> new ArrayList<>()).add(recipeId);
            }
        }

        InfoToDeleteDTO infoFoodie = new InfoToDeleteDTO(recipesId, recipesByChefId);
        lowLoadManager.addTask(Task.TaskType.SET_COUNTERS_FOODIE_DELETE, infoFoodie);

        foodieRepository.delete(foodie);
    }



    /**
     * Add a recipe to foodie's favourites
     * @param foodieId - id of the foodie
     * @param recipeId - the recipe id
     */
    @Transactional
    public void saveRecipe(String foodieId, String recipeId) {

        Foodie foodie = foodieRepository.findById(foodieId)
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        List<FoodieRecipeSummary> recipes = foodie.getSavedRecipes();

        if(recipes != null){
            boolean alreadySaved = recipes.stream()
                    .anyMatch(saved -> saved.getId().equals(recipeId));

            if(alreadySaved){
                throw  new DataIntegrityViolationException("Recipe already saved");
            }
        }

        RecipeMongo recipe = recipeRepository.findApprovedById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe to save not found"));

        FoodieRecipeSummary fullRecipe = recipeUtilityFunctions.entityToReducedRecipe(recipe);

        foodieRepository.addRecipeToFavourites(foodieId, recipeId, fullRecipe);

        ChefRecipeSummary chefRecipe = recipeUtilityFunctions.recipeToChefRecipe(recipe);
        lowLoadManager.addTask(Task.TaskType.SET_COUNTERS_ADD_FAVOURITE, chefRecipe, recipe.getChef().getId());
    }


    /**
     * Remove a recipe from foodie's favourites list
     * @param recipeId - the recipe id
     */
    @Transactional
    public void removeSavedRecipe(String recipeId) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        if (foodie.getSavedRecipes() == null) {
            throw new NoSuchElementException("Recipe not found for the specified foodie");
        }

        String targetChefId = null;
        for (FoodieRecipeSummary recipe : foodie.getSavedRecipes()) {
            if (recipe.getId().equals(recipeId)) {
                targetChefId = recipe.getChef().getId();
                foodieRepository.removeRecipeFromFavourites(foodie.getId(), recipeId);
                break;
            }
        }

        if (targetChefId != null) {
            lowLoadManager.addTask(Task.TaskType.SET_COUNTERS_REMOVE_FAVOURITE, recipeId, targetChefId);
        } else {
            throw new  NoSuchElementException("Recipe not found for the specified foodie");
        }
    }


    /**
     * Show foodie's favourites
     * @param category - category of the recipe
     * @param numPage - paging
     * @return The page with the list of favourites
     */
    public SliceRecipeDTO<FoodiePreviewRecipeDTO> getRecipeByCategory(String category, int numPage) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        if (foodie.getSavedRecipes() == null) {
            throw new NoSuchElementException("Recipe not found for the specified foodie");
        }

        numPage = (numPage < 0) ? 0 : numPage;
        if (!FOODIE_FILTERS.contains(category)) {
            category = "saving-date";
        }

        List<FoodieRecipeSummary> recipesPreview = new ArrayList<>();
        if (CATEGORIES.contains(category)) {
            for (FoodieRecipeSummary recipe : foodie.getSavedRecipes()) {
                if (recipe.getCategory().equals(category)) {
                    recipesPreview.add(recipe);
                }
            }
        } else if (DIFFICULTIES.contains(category)) {
            for (FoodieRecipeSummary recipe : foodie.getSavedRecipes()) {
                if (recipe.getDifficulty().equals(category)) {
                    recipesPreview.add(recipe);
                }
            }
        } else if (category.equals("saving-date")) {
            recipesPreview.addAll(foodie.getSavedRecipes());
        }

        int start = (numPage - 1) * pageSizeFoodie;
        if (start >= recipesPreview.size()) {
            throw new IllegalArgumentException("Invalid page number");
        }

        recipesPreview.sort(Comparator.comparing(FoodieRecipeSummary::getSavingDate).reversed());
        int end = Math.min(start + pageSizeFoodie, recipesPreview.size());

        boolean hasNext = recipesPreview.size() > numPage * pageSizeFoodie;
        boolean hasPrevious = numPage > 1;
        List<FoodieRecipeSummary> recipes = recipesPreview.subList(start, end);

        List<FoodiePreviewRecipeDTO> content = recipeUtilityFunctions.foodieSummaryToUserPreview(recipes);
        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);

    }

    /**
     * Method to get a specific recipe from a foodie's saved list
     * @param id - the recipe ID
     * @return the requested recipe
     */
    public ShowRecipeDTO getRecipeFoodieById(String id){
        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        Optional<RecipeMongo> recipe = recipeRepository.findById(id);

        if(recipe.isEmpty()){
            foodieRepository.removeRecipeFromFavourites(foodie.getId(), id);
            throw new NoSuchElementException("Recipe not found");
        } else if (recipe.get().getStatus().equals("PENDING")) {
            throw new NoSuchElementException("The recipe is not found");
        }

        return recipeUtilityFunctions.EntityToDto(recipe.get());
    }

    /**
     * Get the chef by its surname
     * @param chefSurname - chef surname
     * @return - the chef
     */
    public List<ChefPreviewDTO> getChefList (String chefSurname){
        List<Chef> chefs = chefRepository.findBySurnameContainingIgnoreCase(chefSurname);

        if(chefs.isEmpty()){
            throw new NoSuchElementException("Not matching chefs found");
        }
        return chefConvertions.chefModelToChefDTO(chefs);
    }

    /**
     * Ranking with top 3 chefs
     * @return the top chef for each category in the application
     */
    public List<TopChefDTO> getTopChef() {

        return chefNeo4jRepository.findTop3ChefsByCategory(CATEGORIES);
    }

}