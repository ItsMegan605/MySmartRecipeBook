package it.unipi.MySmartRecipeBook.service;

import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.*;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.FoodiePreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.RecipeSuggestionDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.*;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.Mongo.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.ChefNeo4jRepository;
import it.unipi.MySmartRecipeBook.repository.Neo4j.RecipeNeo4jRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import it.unipi.MySmartRecipeBook.event.Task;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.FoodieUtilityFunctions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Foodie service that handles foodie's business logic operations
 */
@Service
public class FoodieService {

    @Value("${app.recipe.pag-size-foodie:5}")
    private int pageSizeFoodie;

    private final RecipeNeo4jRepository recipeNeo4jRepository;
    private final RecipeUtilityFunctions recipeUtilityFunctions;
    private final FoodieRepository foodieRepository;
    private final RecipeMongoRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FoodieUtilityFunctions foodieConversions;
    private final LowLoadManager lowLoadManager;
    private final ChefNeo4jRepository chefNeo4jRepository;
    private final ChefUtilityFunctions chefConversions;
    private final ChefRepository chefRepository;

    public FoodieService(FoodieRepository foodieRepository, RecipeMongoRepository recipeRepository,
                         PasswordEncoder passwordEncoder, FoodieUtilityFunctions foodieConversions,
                         LowLoadManager lowLoadManager, RecipeUtilityFunctions recipeUtilityFunctions,
                         ChefNeo4jRepository chefNeo4jRepository, ChefUtilityFunctions chefConversions,
                         ChefRepository chefRepository, RecipeNeo4jRepository recipeNeo4jRepository) {
        this.foodieRepository = foodieRepository;
        this.recipeRepository = recipeRepository;
        this.passwordEncoder = passwordEncoder;
        this.foodieConversions = foodieConversions;
        this.lowLoadManager = lowLoadManager;
        this.recipeUtilityFunctions = recipeUtilityFunctions;
        this.chefNeo4jRepository = chefNeo4jRepository;
        this.chefConversions = chefConversions;
        this.chefRepository = chefRepository;
        this.recipeNeo4jRepository = recipeNeo4jRepository;
    }

    /**
     * Retrieves the foodie's personal information.
     * @return a {@link RegisteredUserInfoDTO} containing all the foodie's personal information
     * @throws NoSuchElementException if the foodie is not found
     */
    public RegisteredUserInfoDTO getById() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        return foodieConversions.entityToFoodieDTO(foodie);
    }


    /**
     * Updates an authenticated foodie's personal information. Supported fields for update are name, surname, email, password and birthdate.
     * For security reasons, the username cannot be modified.
     * @param changeInfoDto a {@link UpdateFoodieDTO} containing the personal information the foodie wants to change
     * @return a {@link RegisteredUserInfoDTO} containing the updated chef's personal information
     * @throws NoSuchElementException if the foodie is not found
     * @throws IllegalArgumentException if the {@link UpdateFoodieDTO} is null or empty
     */
    public RegisteredUserInfoDTO updateFoodie(UpdateFoodieDTO changeInfoDto) {

        if(changeInfoDto == null || changeInfoDto.isEmpty()){
            throw new IllegalArgumentException("Invalid parameters");
        }

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        boolean modified = false;

        if (changeInfoDto.getName() != null && StringUtils.hasText(changeInfoDto.getName())) {
            foodie.setName(changeInfoDto.getName());
            modified = true;
        }

        if (changeInfoDto.getSurname() != null && StringUtils.hasText(changeInfoDto.getSurname())) {
            foodie.setSurname(changeInfoDto.getSurname());
            modified = true;
        }

        if (changeInfoDto.getEmail() != null && StringUtils.hasText(changeInfoDto.getEmail())) {
            foodie.setEmail(changeInfoDto.getEmail());
            modified = true;
        }

        if (changeInfoDto.getPassword() != null && StringUtils.hasText(changeInfoDto.getPassword())) {
            foodie.setPassword(passwordEncoder.encode(changeInfoDto.getPassword()));
            modified = true;
        }

        if (changeInfoDto.getBirthdate() != null){
            foodie.setBirthdate(changeInfoDto.getBirthdate());
            modified = true;
        }

        if (modified) {
            foodieRepository.save(foodie);
        }
        return foodieConversions.entityToFoodieDTO(foodie);
    }


    /**
     * Deletes the currently authenticated foodie's profile. Asynchronously, the chef and recipes counters are updated
     * to account for the removal of the foodie's profile and all their saved recipes.
     * @throws NoSuchElementException if the authenticated foodie is not found in the database
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
     * Adds a preview of the selected recipe to the authenticated foodie's favorites list.
     * Asynchronously, the recipe's total saves and the corresponding chef's counters are updated.
     * @param recipeId the unique identifier of the recipe to save
     * @throws NoSuchElementException if the recipe or the foodie is not found
     * @throws DataIntegrityViolationException if the recipe has already been saved by the foodie
     */
    @Transactional
    public void saveRecipe(String recipeId) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        RecipeMongo recipe = recipeRepository.findApprovedById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        List<FoodieRecipeSummary> recipes = foodie.getSavedRecipes();
        if(recipes != null){
            boolean alreadySaved = recipes.stream()
                    .anyMatch(saved -> saved.getId().equals(recipeId));

            if(alreadySaved){
                throw  new DataIntegrityViolationException("Recipe already saved");
            }
        }

        FoodieRecipeSummary fullRecipe = recipeUtilityFunctions.entityToReducedRecipe(recipe);
        foodieRepository.addRecipeToFavourites(authFoodie.getId(), recipeId, fullRecipe);

        lowLoadManager.addTask(Task.TaskType.SET_COUNTERS_NEW_FAVOURITE, fullRecipe.getId(), recipe.getChef().getId());
    }


    /**
     * Removes the preview of the selected recipe from the authenticated foodie's favorites list.
     * Asynchronously, the recipe's total saves and the corresponding chef's counters are updated.
     * @param recipeId the unique identifier of the recipe to remove
     * @throws NoSuchElementException if the foodie or the recipe is not found, or if the recipe is not present among the saved ones
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
            throw new NoSuchElementException("Recipe not found among the saved one");
        }
    }


    /**
     * Retrieves the foodie's favorite recipes filtered by a specified category, difficulty, or saving date.
     * The results are sorted in descending order by saving date and paginated.
     * @param filter the filtering criterion
     * @param numPage the number of the page to retrieve
     * @return a {@link SliceRecipeDTO} containing the paginated recipe previews, along with two boolean values indicating
     * the existence of previous or next pages
     * @throws NoSuchElementException if the foodie is not found
     * @throws IllegalArgumentException if the page number is zero or negative, or if the filter is invalid
     */
    public SliceRecipeDTO<FoodiePreviewRecipeDTO> getRecipeByCategory(String filter, int numPage) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        if (foodie.getSavedRecipes() == null) {
            return new SliceRecipeDTO<>(new ArrayList<>(), false, false);
        }

        if(numPage <= 0 || !FOODIE_FILTERS.contains(filter)){
            throw new IllegalArgumentException("Invalid parameters");
        }

        List<FoodieRecipeSummary> recipesPreview = new ArrayList<>();
        if (CATEGORIES.contains(filter)) {
            for (FoodieRecipeSummary recipe : foodie.getSavedRecipes()) {
                if (recipe.getCategory().equals(filter)) {
                    recipesPreview.add(recipe);
                }
            }
        } else if (DIFFICULTIES.contains(filter)) {
            for (FoodieRecipeSummary recipe : foodie.getSavedRecipes()) {
                if (recipe.getDifficulty().equals(filter)) {
                    recipesPreview.add(recipe);
                }
            }
        } else if (filter.equals("saving-date")) {
            recipesPreview.addAll(foodie.getSavedRecipes());
        }

        int start = (numPage - 1) * pageSizeFoodie;
        boolean hasPrevious = numPage > 1;
        if (start >= recipesPreview.size()) {
            return new SliceRecipeDTO<>(new ArrayList<>(), false, hasPrevious);
        }

        recipesPreview.sort(Comparator.comparing(FoodieRecipeSummary::getSavingDate).reversed());

        int end = Math.min(start + pageSizeFoodie, recipesPreview.size());
        boolean hasNext = recipesPreview.size() > numPage * pageSizeFoodie;

        List<FoodieRecipeSummary> recipes = recipesPreview.subList(start, end);
        List<FoodiePreviewRecipeDTO> content = recipeUtilityFunctions.foodieSummaryToUserPreview(recipes);
        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }


    /**
     * Retrieves the detailed information of the specified recipe from the foodie's favorites.
     * If the saved recipe has been deleted from the database, it is automatically removed from the foodie's favorites.
     * @param recipeId the unique identifier of the recipe the foodie wants to visualize
     * @return a {@link ShowRecipeDTO} containing all the detailed information of the recipe
     * @throws NoSuchElementException if the foodie or the recipe is not found, or if the recipe is not in the saved list
     */
    @Transactional
    public ShowRecipeDTO getRecipeFoodieById(String recipeId){
        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Foodie not found"));

        if(foodie.getSavedRecipes() == null){
            throw new NoSuchElementException("Recipe not found");
        }

        boolean found = foodie.getSavedRecipes().stream()
                .anyMatch(recipe -> recipe.getId().equals(recipeId));

        if(!found){
            throw new NoSuchElementException("Recipe not found for the specified foodie");
        }

        Optional<RecipeMongo> recipe = recipeRepository.findApprovedById(recipeId);

        if(recipe.isEmpty()){
            foodieRepository.removeRecipeFromFavourites(foodie.getId(), recipeId);
            throw new NoSuchElementException("Recipe not found");
        }

        return recipeUtilityFunctions.entityToDto(recipe.get());
    }


    /**
     * Retrieves the list of matching chefs by their surname.
     * @param chefSurname the target surname to search for
     * @return a list of {@link ChefPreviewDTO} containing the previews of the matching chefs,
     * or an empty list if no matches are found
     */
    public List<ChefPreviewDTO> getChefList (String chefSurname){

        List<Chef> chefs = chefRepository.findBySurnameContainingIgnoreCase(chefSurname);
        if(chefs == null || chefs.isEmpty()){
            return new ArrayList<>();
        }

        return chefConversions.chefModelToChefDTO(chefs);
    }


    /**
     * Retrieves the ranking of the top 3 chefs for each available recipe category.
     * The ranking is determined by the volume of recipes written by a chef within a specific category.
     * @return a list of {@link TopChefDTO} containing the name of the top chefs grouped by category
     */
    public List<TopChefDTO> getTopChef() {

        return chefNeo4jRepository.findTop3ChefsByCategory(CATEGORIES);
    }


    /**
     * Retrieves a list of recipes that are similar to the specified target recipe.
     * The similarity is calculated based on the number of shared ingredients.
     * @param recipeId the unique identifier of the target recipe
     * @return a list of up to three {@link RecipeSuggestionDTO} containing the most similar recipes,
     * ordered by the highest number of shared ingredients
     * @throws NoSuchElementException if the recipe is not found or if it has not been approved yet
     */
    public List<RecipeSuggestionDTO> getSimilarRecipes(String recipeId){

        RecipeMongo recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        if(recipe.getStatus().equals("PENDING")){
            throw new NoSuchElementException("Recipe not found");
        }

        return recipeNeo4jRepository.findSimilarRecipes(recipeId);
    }

}