package it.unipi.MySmartRecipeBook.service;

import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.*;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.dto.users.*;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.*;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.repository.Mongo.*;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.event.Task;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;
import java.util.NoSuchElementException;

//import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;

/**
 * Service Function for the chef of the application
 * where the logic of the different operations is handled.
 * At first the parameters, such as repositories  and utility functions are declared
 */
@Service
public class ChefService {


    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeChef;


    private final ChefRepository chefRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChefUtilityFunctions chefConvertions;
    private final RecipeUtilityFunctions recipeConvertions;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeMongoRepository;
    private final LowLoadManager lowLoadManager;
    private final IngredientService ingredientService;

    public ChefService(ChefRepository chefRepository, ChefUtilityFunctions chefConvertions,
                       RecipeUtilityFunctions recipeConvertions, PasswordEncoder passwordEncoder,
                       AdminRepository adminRepository, RecipeMongoRepository recipeMongoRepository,
                       LowLoadManager lowLoadManager, IngredientService ingredientService) {
        this.chefRepository = chefRepository;
        this.chefConvertions = chefConvertions;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.recipeMongoRepository = recipeMongoRepository;
        this.lowLoadManager = lowLoadManager;
        this.ingredientService = ingredientService;
        this.recipeConvertions = recipeConvertions;
    }


    /**
     * Retrieve chef's information
     * @param username Gets the chef's username
     * @return if the chef exists or not to gather his/her information
     * @throws NoSuchElementException if the chef doesn't exist
     */
    public RegisteredUserInfoDTO getByUsername(String username) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        return chefConvertions.chefToChefInfo(chef);
    }


    /**
     * This function allows a chef to change his/her personal information, in particular
     * one or more among the following fields: Email, password and birthday
     * We don't allow a chef to change his/her username, name and surname for security reasons
     * @param dto We get the dto for the chef and check the authentication parameters
     * @return if the chef exists we return the updated chef's information
     * @throws NoSuchElementException if the chef doesn't exist
     */
    public RegisteredUserInfoDTO updateChef(UpdateChefDTO dto) {

        if(dto == null || dto.isEmpty()){
            throw new IllegalArgumentException("Invalid parameters");
        }

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        boolean modified = false;

        if (dto.getEmail() != null && StringUtils.hasText(dto.getEmail())){
            chef.setEmail(dto.getEmail());
            modified = true;
        }

        if (dto.getPassword() != null && StringUtils.hasText(dto.getPassword())){
            chef.setPassword(passwordEncoder.encode(dto.getPassword()));
            modified = true;
        }

        if (dto.getBirthdate() != null){
            chef.setBirthdate(dto.getBirthdate());
            modified = true;
        }

        if(modified){
            chefRepository.save(chef);
        }
        return chefConvertions.chefToChefInfo(chef);
    }

    /**
     * Delete chef's profile
     * @throws NoSuchElementException if the chef doesn't exist
     */
    @Transactional
    public void deleteChef() {

        UserPrincipal loggedChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(loggedChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        recipeMongoRepository.deleteAllByChefId(loggedChef.getId());
        chefRepository.delete(chef);

        lowLoadManager.addTask(Task.TaskType.DELETE_CHEF_RECIPE, loggedChef.getId());
    }

    // TODO: TESTARE
    /**
     * Function called by the chef to write a new recipe: the recipe is on a waiting list at the
     * beginning waiting for admin's approval.
     * @param recipeDTO DTO with all the mandatory fields inserted by the chef when he writes the recipe
     * @throws NoSuchElementException if the chef is not found, if the admin is not found and if one of the fields is wrong/missing
     * @return DTO with a recipe preview to show the chef while he/she waits for the approval
     */
    @Transactional
    public PendingRecipeChefDTO createRecipe(CreateRecipeDTO recipeDTO) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if (recipeDTO.getCategory() == null || !CATEGORIES.contains(recipeDTO.getCategory())) {
            throw new IllegalArgumentException("Invalid or missing category");
        }

        if (recipeDTO.getDifficulty() == null || !DIFFICULTIES.contains(recipeDTO.getDifficulty())) {
            throw new IllegalArgumentException("Invalid or missing difficulty");
        }

        List<IngredientDTO> ingredients = recipeDTO.getIngredients();
        for(IngredientDTO ingredient : ingredients) {

            String ingredientName = ingredient.getName();

            if(!ingredientService.isValidIngredient(ingredientName)){
                throw new IllegalArgumentException("'" + ingredientName + "': invalid ingredient");
            }
            else if(!ingredient.checkQuantity()){
                throw new IllegalArgumentException("Invalid or missing quantity");
            }

        }

        if(!recipeDTO.validPrepTime()) {
            throw new IllegalArgumentException("Invalid or missing preparation time");
        }

        Admin admin = adminRepository.findByUsername("admin");

        if (admin == null) {
            throw new NoSuchElementException("Admin not found");
        }

        ChefInfoDTO chefDTO = new ChefInfoDTO(chef.getId(), chef.getName(), chef.getSurname());
        RecipeMongo recipeToAdd = recipeConvertions.dtoToModel(recipeDTO, chefDTO);
        RecipeMongo recipeAdded = recipeMongoRepository.save(recipeToAdd);

        AdminPendingRecipe savedRecipe = recipeConvertions.createBaseRecipe(recipeDTO, chefDTO, recipeAdded.getId());
        adminRepository.addRecipeToApprovals(admin.getId(), savedRecipe);

        PendingRecipe chefRecipe = recipeConvertions.recipeToChefRecipe(savedRecipe, recipeAdded.getId());
        chefRepository.addRecipeToWaiting(chef.getId(), chefRecipe);

        return recipeConvertions.baseToChefDTO(savedRecipe);
    }

    /**
     * function to delete a recipe: once deleted it must update the total recipes of a chef and later
     * update the user's SmartFridge
     * @param recipeId gets the recipe's ID number
     * @throws NoSuchElementException if the chef or recipe is not found
     */
    @Transactional
    public void deleteRecipe(String recipeId) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        List<ChefRecipeSummary> newRecipes = chef.getNewRecipes();

        if (newRecipes == null) {
            throw new NoSuchElementException("No recipes found");
        }

        RecipeMongo deletedRecipe = recipeMongoRepository.deleteRecipeById(recipeId);
        if(deletedRecipe == null){
            throw new NoSuchElementException("Recipe not found");
        }

        boolean findRecipe = false;
        for (ChefRecipeSummary recipe : newRecipes) {
            if (recipe.getId().equals(recipeId)) {

                chef.setTotalSaves(chef.getTotalSaves() - recipe.getNumSaves());
                newRecipes.remove(recipe);

                if(chef.getOldRecipes() != null){
                    String oldRecipeId = chef.getOldRecipes().remove(0);
                    RecipeMongo recipeMongo = recipeMongoRepository.findById(oldRecipeId)
                            .orElseThrow(() -> new NoSuchElementException("Recipe not found"));
                    ChefRecipeSummary reducedRecipe = recipeConvertions.recipeToChefRecipe(recipeMongo);
                    chef.getNewRecipes().add(reducedRecipe);
                }

                findRecipe = true;
                break;
            }
        }

        if(!findRecipe){
            for(String oldRecipeId : chef.getOldRecipes()){
                if(oldRecipeId.equals(recipeId)){
                    chef.setTotalSaves(chef.getTotalSaves() - deletedRecipe.getNumSaves());
                    chef.getOldRecipes().remove(oldRecipeId);
                    break;
                }
            }
        }


        List<ChefRecipeSummary> popularRecipes = chef.getPopularRecipes();
        if(popularRecipes != null){
            for(ChefRecipeSummary popularRecipe : popularRecipes){
                if(popularRecipe.getId().equals(recipeId)){
                    chef.getPopularRecipes().remove(popularRecipe);
                    break;
                }
            }
        }

        chef.setTotalRecipes(chef.getTotalRecipes() - 1);
        chefRepository.save(chef);

        lowLoadManager.addTask(Task.TaskType.DELETE_RECIPE, recipeId);
    }

    // TODO: da testare
    /**
     * Remove a recipe from the list of recipes waiting to be confirmed
     * @param recipeId gets the recipe ID of the recipe that is waiting to be approved
     * @throws NoSuchElementException
     * Updates the Repository
     */
    @Transactional
    public void removeRecipe(String recipeId) {

        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(chef1.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if(chef.getRecipesToConfirm() == null){
            throw new NoSuchElementException("No recipes waiting to be confirmed");
        }

        chefRepository.removeRecipeFromWaiting(chef.getId(), recipeId);

        Admin admin = adminRepository.findByUsername("admin");
        if(admin == null){
            throw new NoSuchElementException("Admin not found");
        }

        adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId);
        recipeMongoRepository.deleteById(recipeId);
    }

    /**
     * Function to show the total recipes to a chef
     * @param pageNumber Number of the page, each page has 5 recipes
     * @return the recipe's details
     *
     */
    public SliceRecipeDTO<ChefPreviewRecipeDTO> showRecipes (int pageNumber){

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid parameters");
        }


        int start = (pageNumber-1)*pageSizeChef;
        int end = pageNumber*pageSizeChef;
        List<ChefPreviewRecipeDTO> content;
        boolean hasPrevious = true;
        if(pageNumber <= 3){

            if (chef.getNewRecipes() == null || chef.getNewRecipes().isEmpty()) {
                return new SliceRecipeDTO<>(null, false, false);
            }

            content = recipeConvertions.ChefListToSummaryList(chef.getNewRecipes().subList(start, end));
            hasPrevious = pageNumber != 1;
        }

        else{

            List<String> oldRecipesIds = chef.getOldRecipes().subList(start, end);
            List<RecipeMongo> recipes = recipeMongoRepository.findByIdIn(oldRecipesIds);
            content = recipeConvertions.MongoListToChefPreview(recipes);
        }

        boolean hasNext = chef.getTotalRecipes() > end;
        return  new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }

    /**
     * Method to show the most popular recipes from a chef
     * @param pageNumber - paging
     * @return - paging with the list of recipes
     */
    public SliceRecipeDTO<ChefPreviewRecipeDTO> showPopularRecipes(int pageNumber){

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid parameters");
        }

        if(chef.getPopularRecipes() == null || chef.getPopularRecipes().isEmpty()) {
            throw new NoSuchElementException("No popular recipes");
        }

        int start = (pageNumber-1)*pageSizeChef;
        int end = pageNumber*pageSizeChef;

        boolean hasPrevious = pageNumber != 1;
        boolean hasNext = chef.getPopularRecipes().size() > end;

        List<ChefRecipeSummary> chefList = chef.getPopularRecipes().subList(start, end);
        List<ChefPreviewRecipeDTO> previewList = recipeConvertions.ChefListToSummaryList(chefList);
        return new SliceRecipeDTO<>(previewList, hasNext, hasPrevious);
    }

    /**
     * Method to show a chef his/her pending recipes
     * @param pageNumber - paging
     * @return the page with the list of pending recipes
     */
    public SliceRecipeDTO<PendingRecipeChefDTO> showPendingRecipes(int pageNumber) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        if (chef.getRecipesToConfirm() == null || chef.getRecipesToConfirm().isEmpty()) {
            return new SliceRecipeDTO<>(null, false, false);
        }

        List<PendingRecipe> pendingRecipes = chef.getRecipesToConfirm();

        int start = (pageNumber - 1) * pageSizeChef;
        int end = Math.min(pageNumber * pageSizeChef, pendingRecipes.size());

        if (start >= pendingRecipes.size()) {
            return new SliceRecipeDTO<>(null, false, true);
        }
        List<PendingRecipeChefDTO> content = recipeConvertions.ChefPreviewToPendingChefRecipe(
                pendingRecipes.subList(start, end));

        boolean hasPrevious = pageNumber > 1;
        boolean hasNext = pendingRecipes.size() > end;

        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }


    /**
     * Method to get the Recipe details
     * @param recipeId - recipe id
     * @return the recipe
     */
    public ShowRecipeDTO getRecipeDetails(String recipeId){
        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        RecipeMongo recipe = recipeMongoRepository.findById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        if (!recipe.getStatus().equals("PENDING")) {
            throw new NoSuchElementException("Not pending recipe");
        }

        if(!recipe.getChef().getId().equals(chef.getId())){
            throw new NoSuchElementException("Recipe not found");
        }

        return recipeConvertions.EntityToDto(recipe);
    }
}
