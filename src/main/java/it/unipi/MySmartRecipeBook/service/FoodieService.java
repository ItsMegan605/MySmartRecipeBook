package it.unipi.MySmartRecipeBook.service;

import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.*;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.FoodiePreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ShowRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.*;
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

    private final RecipeUtilityFunctions recipeUtilityFunctions;
    private final FoodieRepository foodieRepository;
    private final RecipeMongoRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FoodieUtilityFunctions usersConvertions;
    private final LowLoadManager lowLoadManager;
    private final ChefNeo4jRepository chefNeo4jRepository;
    private final ChefUtilityFunctions chefConvertions;
    private final ChefRepository chefRepository;

    public FoodieService(FoodieRepository foodieRepository, RecipeMongoRepository recipeRepository,
                         PasswordEncoder passwordEncoder, FoodieUtilityFunctions usersConvertions,
                         LowLoadManager lowLoadManager, RecipeUtilityFunctions recipeUtilityFunctions,
                         ChefNeo4jRepository chefNeo4jRepository, ChefUtilityFunctions chefConvertions, ChefRepository chefRepository) {
        this.foodieRepository = foodieRepository;
        this.recipeRepository = recipeRepository;
        this.passwordEncoder = passwordEncoder;
        this.usersConvertions = usersConvertions;
        this.lowLoadManager = lowLoadManager;

        this.recipeUtilityFunctions = recipeUtilityFunctions;
        this.chefNeo4jRepository = chefNeo4jRepository;
        this.chefConvertions = chefConvertions;
        this.chefRepository = chefRepository;
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

        return usersConvertions.entityToFoodieDTO(foodie);
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
        return usersConvertions.entityToFoodieDTO(foodie);
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

    // TODO: da ricontrollare gestione asincrona
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
     * Removes the preview of the selected recipe to the authenticated foodie's favorites list.
     * Asynchronously, the recipe's total saves and the corresponding chef's counters are updated.
     * @param recipeId the unique identifier of the recipe to remove
     * @throws NoSuchElementException if the foodie or the foodie is not found, or if the recipe is not present in the saved list
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

        numPage = Math.max(numPage, 1);
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