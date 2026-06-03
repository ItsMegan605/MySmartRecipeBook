package it.unipi.MySmartRecipeBook.service;

import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.*;

import org.springframework.dao.DuplicateKeyException;
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


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

//import org.bson.types.ObjectId;
import org.springframework.util.StringUtils;

/**
 * Chef service that handles chef's business logic operations
 */
@Service
public class ChefService {

    @Value("${app.recipe.pag-size-chef:5}")
    private int pageSizeChef;

    private final ChefRepository chefRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChefUtilityFunctions chefConversions;
    private final RecipeUtilityFunctions recipeConversions;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeMongoRepository;
    private final LowLoadManager lowLoadManager;
    private final IngredientService ingredientService;

    public ChefService(ChefRepository chefRepository, ChefUtilityFunctions chefConversions,
                       RecipeUtilityFunctions recipeConversions, PasswordEncoder passwordEncoder,
                       AdminRepository adminRepository, RecipeMongoRepository recipeMongoRepository,
                       LowLoadManager lowLoadManager, IngredientService ingredientService) {
        this.chefRepository = chefRepository;
        this.chefConversions = chefConversions;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.recipeMongoRepository = recipeMongoRepository;
        this.lowLoadManager = lowLoadManager;
        this.ingredientService = ingredientService;
        this.recipeConversions = recipeConversions;
    }


    /**
     * Retrieves the chef's personal information.
     * @return a {@link RegisteredUserInfoDTO} containing all the chef's personal information
     * @throws NoSuchElementException if the chef is not found or is not in an "APPROVED" state
     */
    public RegisteredUserInfoDTO getByUsername() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findApprovedById(authFoodie.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        return chefConversions.chefToChefInfo(chef);
    }


    /**
     * Updates an authenticated chef's personal information. Supported fields for update are email, password and birthdate.
     * For security reasons, the username, name, and surname cannot be modified.
     * @param changeInfoDto a {@link UpdateChefDTO} containing the personal information the chef wants to change
     * @return a {@link RegisteredUserInfoDTO} containing the updated chef's personal information
     * @throws NoSuchElementException if the chef is not found
     * @throws IllegalArgumentException if the {@link UpdateChefDTO} is null or empty
     */
    public RegisteredUserInfoDTO updateChef(UpdateChefDTO changeInfoDto) {

        if(changeInfoDto == null || changeInfoDto.isEmpty()){
            throw new IllegalArgumentException("Invalid parameters");
        }

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        boolean modified = false;

        if (changeInfoDto.getEmail() != null && StringUtils.hasText(changeInfoDto.getEmail())){
            chef.setEmail(changeInfoDto.getEmail());
            modified = true;
        }

        if (changeInfoDto.getPassword() != null && StringUtils.hasText(changeInfoDto.getPassword())){
            chef.setPassword(passwordEncoder.encode(changeInfoDto.getPassword()));
            modified = true;
        }

        if (changeInfoDto.getBirthdate() != null){
            chef.setBirthdate(changeInfoDto.getBirthdate());
            modified = true;
        }

        if(modified){
            chefRepository.save(chef);
        }
        return chefConversions.chefToChefInfo(chef);
    }


    /**
     * Deletes the currently authenticated chef's profile, removing all its recipes from the DB
     * and removing any recipes from the admin's pending list.
     * The chef and its corresponding recipes are then asynchronously deleted from the graph DB.
     * @throws NoSuchElementException if the chef is not found
     */
    @Transactional
    public void deleteChef() {

        UserPrincipal loggedChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findApprovedById(loggedChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        List<String> ids = new ArrayList<>();
        if(chef.getNewRecipes() != null && !chef.getNewRecipes().isEmpty()){
            for(ChefRecipeSummary recipe : chef.getNewRecipes()){
                ids.add(recipe.getId());
            }
        }

        if(chef.getOldRecipes() != null && !chef.getOldRecipes().isEmpty()){
                ids.addAll(chef.getOldRecipes());
        }


        if(chef.getRecipesToConfirm() != null) {
            List<String> recipeToRemove = new ArrayList<>();
            for(PendingRecipe recipe : chef.getRecipesToConfirm()) {
                ids.add(recipe.getId());
                recipeToRemove.add(recipe.getId());
            }
            recipeMongoRepository.deleteByIdIn(ids);
            Admin admin = adminRepository.findByUsername("admin");
            adminRepository.removeRecipesFromApprovals(admin.getId(), recipeToRemove);
        }

        chefRepository.delete(chef);
        lowLoadManager.addTask(Task.TaskType.DELETE_CHEF_PROFILE_NEO4J, "0", loggedChef.getId());
    }


    /**
     * Creates a new recipe for the currently authenticated chef. The recipe is saved to the database
     * with a "PENDING" status and is added to both the admin's and the chef's pending lists,
     * waiting for admin's approval.
     * @param recipeDTO a {@link CreateRecipeDTO} containing all the mandatory fields provided by the chef
     * @return a {@link PendingRecipeChefDTO} providing a preview of the newly created recipe
     * @throws NoSuchElementException if the chef or the admin is not found
     * @throws IllegalArgumentException if the category, difficulty, ingredients, quantity or preparation time are missing or invalid
     */
    @Transactional
    public PendingRecipeChefDTO createRecipe(CreateRecipeDTO recipeDTO) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findApprovedById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if (recipeDTO.getCategory() == null || !CATEGORIES.contains(recipeDTO.getCategory())) {
            throw new IllegalArgumentException("Invalid or missing category");
        }

        if (recipeDTO.getDifficulty() == null || !DIFFICULTIES.contains(recipeDTO.getDifficulty())) {
            throw new IllegalArgumentException("Invalid or missing difficulty");
        }
        List<IngredientDTO> ingredients = recipeDTO.getIngredients();
        ingredients.forEach(i -> i.setName(i.getName().toLowerCase()));
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
        RecipeMongo recipeToAdd = recipeConversions.dtoToModel(recipeDTO, chefDTO);
        RecipeMongo recipeAdded = null;
       try {
           recipeAdded = recipeMongoRepository.save(recipeToAdd);
       } catch(DuplicateKeyException e){
            throw new IllegalArgumentException("Recipe already exists");
       };


        AdminPendingRecipe savedRecipe = recipeConversions.createBaseRecipe(recipeDTO, chefDTO, recipeAdded.getId());
        adminRepository.addRecipeToApprovals(admin.getId(), savedRecipe);

        PendingRecipe chefRecipe = recipeConversions.recipeToChefRecipe(savedRecipe, recipeAdded.getId());
        chefRepository.addRecipeToWaiting(chef.getId(), chefRecipe);

        return recipeConversions.baseToChefDTO(savedRecipe);
    }


    /**
     * Deletes a recipe belonging to the authenticated chef. The lists of new and old recipes are handled accordingly:
     * if the recipe is one of the old ones, its id is simply removed from the list; however, if it is one of the newest 15,
     * it is removed from the new recipes list and the most recent among the old ones is inserted into it (using the appropriate conversions).
     * If the recipe is among the popular ones, it is removed from that list as well. The chef's total number of saves and the total number of
     * recipes are updated accordingly. Asynchronously, the recipe is removed from the graph DB.
     * @param recipeId the unique identifier of the recipe to delete
     * @throws NoSuchElementException if the chef or recipe is not found, or if the recipe does not belong to the authenticated chef
     */
    @Transactional
    public void deleteRecipe(String recipeId) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findApprovedById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));


        RecipeMongo recipeToDelete = recipeMongoRepository.findApprovedById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        if(!recipeToDelete.getChef().getId().equals(chef.getId())) {
            throw new NoSuchElementException("Recipe not found");
        }

        recipeMongoRepository.delete(recipeToDelete);

        int totSaves = chef.getTotalSaves() == null ? 0 : chef.getTotalSaves();
        int recipeSaves = recipeToDelete.getNumSaves() == null ? 0 : recipeToDelete.getNumSaves();
        chef.setTotalSaves(Math.max(0, totSaves - recipeSaves));

        List<ChefRecipeSummary> newRecipes = chef.getNewRecipes() == null ? new ArrayList<>() : chef.getNewRecipes();
        boolean recipeFound = newRecipes.removeIf(recipe -> recipe.getId().equals(recipeId));

        if(recipeFound) {
            if(chef.getOldRecipes() != null && !chef.getOldRecipes().isEmpty()) {
                String oldRecipeId = chef.getOldRecipes().remove(0);
                RecipeMongo recipeMongo = recipeMongoRepository.findById(oldRecipeId)
                        .orElseThrow(() -> new NoSuchElementException("Recipe not found"));
                ChefRecipeSummary reducedRecipe = recipeConversions.recipeToChefRecipe(recipeMongo);
                chef.getNewRecipes().add(reducedRecipe);
            }
        }
        else {
            if (chef.getOldRecipes() != null) {
                chef.getOldRecipes().remove(recipeId);
            }
        }

        if(recipeToDelete.getNumSaves() != null &&  recipeToDelete.getNumSaves() >= 40 &&
            chef.getPopularRecipes() != null && !chef.getPopularRecipes().isEmpty()) {
            chef.getPopularRecipes().removeIf(popularRecipe -> popularRecipe.getId().equals(recipeId));
        }

        int totRecipes = chef.getTotalRecipes() == null ? 0 : chef.getTotalRecipes();
        chef.setTotalRecipes(Math.max(0, totRecipes - 1));

        chefRepository.save(chef);
        lowLoadManager.addTask(Task.TaskType.DELETE_RECIPE_NEO4J, recipeId);
    }


    /**
     * Removes a pending recipe from both the admin's and the chef's pending list. The recipe is then deleted from the DB.
     * @param recipeId the unique identifier of the recipe to delete
     * @throws NoSuchElementException if the chef, the admin, or the recipe is not found,
     * if the recipe is not in a "PENDING" state, or if the recipe does not belong to the authenticated chef
     */
    @Transactional
    public void removeRecipe(String recipeId) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findApprovedById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        RecipeMongo recipe = recipeMongoRepository.findById(recipeId)
                    .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        if(!recipe.getStatus().equals("PENDING")){
            throw new NoSuchElementException("Recipe not found");
        }

        if(!recipe.getChef().getId().equals(chef.getId())) {
            throw new NoSuchElementException("Recipe not found");
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
     * Retrieves a paginated list of the preview of the chef's approved recipes, ordered in descending order
     * (from newest to oldest) by creation date.
     * @param pageNumber the requested page number
     * @return a {@link SliceRecipeDTO} containing a preview of the recipes, along with two boolean values indicating
     * the existence of previous or next pages
     * @throws NoSuchElementException if the authenticated chef is not found
     * @throws IllegalArgumentException if the page number is zero or negative
     */
    public SliceRecipeDTO<ChefPreviewRecipeDTO> showRecipes (int pageNumber){

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findApprovedById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid parameters");
        }

        int start = (pageNumber-1)*pageSizeChef;
        int end = pageNumber*pageSizeChef;

        List<ChefPreviewRecipeDTO> content;
        boolean hasPrevious = pageNumber > 1;

        if(pageNumber <= 3){

            if (chef.getNewRecipes() == null || chef.getNewRecipes().isEmpty()) {
                return new SliceRecipeDTO<>(new ArrayList<>(), false, false);
            }

            if (start >= chef.getNewRecipes().size()) {
                return new SliceRecipeDTO<>(new ArrayList<>(), false, hasPrevious);
            }

            int actualEnd = Math.min(end, chef.getNewRecipes().size());
            content = recipeConversions.ChefListToSummaryList(chef.getNewRecipes().subList(start, actualEnd));
        }
        else{

            if (chef.getOldRecipes() == null || chef.getOldRecipes().isEmpty()) {
                return new SliceRecipeDTO<>(new ArrayList<>(), false, true);
            }

            int offset = 3*pageSizeChef;
            if (start -offset >= chef.getOldRecipes().size()) {
                return new SliceRecipeDTO<>(new ArrayList<>(), false, true);
            }

            int actualEnd = Math.min(end-offset, chef.getOldRecipes().size());
            List<String> oldRecipesIds = chef.getOldRecipes().subList(start - offset, actualEnd);
            List<RecipeMongo> recipes = recipeMongoRepository.findByIdIn(oldRecipesIds);
            content = recipeConversions.MongoListToChefPreview(recipes);
        }

        int totalRecipes = chef.getTotalRecipes() == null ? 0 : chef.getTotalRecipes();
        boolean hasNext = totalRecipes > end;
        return  new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }


    /**
     * Retrieves a paginated list of the preview of the chef's popular recipes, ordered in descending order
     * (from the most popular to the least popular).
     * @param pageNumber the requested page number
     * @return a {@link SliceRecipeDTO} containing a preview of the recipes, along with two boolean values indicating
     * the existence of previous or next pages
     * @throws NoSuchElementException if the authenticated chef is not found
     * @throws IllegalArgumentException if the page number is zero or negative
     */
    public SliceRecipeDTO<ChefPreviewRecipeDTO> showPopularRecipes(int pageNumber){

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findApprovedById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if(pageNumber <= 0){
            throw new IllegalArgumentException("Invalid parameters");
        }

        if(chef.getPopularRecipes() == null || chef.getPopularRecipes().isEmpty()) {
            return new SliceRecipeDTO<>(new ArrayList<>(), false, false);
        }

        int start = (pageNumber-1)*pageSizeChef;
        if (start >= chef.getPopularRecipes().size()) {
            return new SliceRecipeDTO<>(new ArrayList<>(), false, true);
        }

        int end = Math.min(pageNumber*pageSizeChef, chef.getPopularRecipes().size());

        boolean hasPrevious = pageNumber > 1;
        boolean hasNext = chef.getPopularRecipes().size() > end;

        List<ChefRecipeSummary> chefList = chef.getPopularRecipes().subList(start, end);
        List<ChefPreviewRecipeDTO> previewList = recipeConversions.ChefListToSummaryList(chefList);
        return new SliceRecipeDTO<>(previewList, hasNext, hasPrevious);
    }


    /**
     * Retrieves a paginated list of the preview of the chef's pending recipes, waiting for the admin's approval,
     * ordered in ascending order (from the oldest to the newest).
     * @param pageNumber the requested page number
     * @return a {@link SliceRecipeDTO} containing a preview of the recipes, along with two boolean values indicating
     * the existence of previous or next pages
     * @throws NoSuchElementException if the authenticated chef is not found
     * @throws IllegalArgumentException if the page number is zero or negative
     */
    public SliceRecipeDTO<PendingRecipeChefDTO> showPendingRecipes(int pageNumber) {

        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findApprovedById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        if (chef.getRecipesToConfirm() == null || chef.getRecipesToConfirm().isEmpty()) {
            return new SliceRecipeDTO<>(new ArrayList<>(), false, false);
        }

        List<PendingRecipe> pendingRecipes = chef.getRecipesToConfirm();

        int start = (pageNumber - 1) * pageSizeChef;
        if (start >= pendingRecipes.size()) {
            return new SliceRecipeDTO<>(new ArrayList<>(), false, true);
        }

        int end = Math.min(pageNumber * pageSizeChef, pendingRecipes.size());

        List<PendingRecipeChefDTO> content = recipeConversions.ChefPreviewToPendingChefRecipe(
                pendingRecipes.subList(start, end));

        boolean hasPrevious = pageNumber > 1;
        boolean hasNext = pendingRecipes.size() > end;

        return new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }


    /**
     * Retrieves the details of a specific recipe.
     * @param recipeId the unique identifier of the recipe to retrieve
     * @return a {@link ShowRecipeDTO} containing the full details of the recipe
     * @throws NoSuchElementException if the chef or the recipe is not found,
     * or if the recipe does not belong to the authenticated chef
     */
    public ShowRecipeDTO getRecipeDetails(String recipeId){
        UserPrincipal authChef = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findApprovedById(authChef.getId())
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));

        RecipeMongo recipe = recipeMongoRepository.findById(recipeId)
                .orElseThrow(() -> new NoSuchElementException("Recipe not found"));

        if(!recipe.getChef().getId().equals(chef.getId())){
            throw new NoSuchElementException("Recipe not found");
        }

        return recipeConversions.EntityToDto(recipe);
    }
}
