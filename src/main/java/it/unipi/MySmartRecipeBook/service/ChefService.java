package it.unipi.MySmartRecipeBook.service;

import static it.unipi.MySmartRecipeBook.utils.parameters.Parameters.*;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.*;
import it.unipi.MySmartRecipeBook.dto.users.*;
import it.unipi.MySmartRecipeBook.dto.ChefRankAnalyticsDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.*;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Admin;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.repository.Mongo.*;
import it.unipi.MySmartRecipeBook.repository.Neo4j.ChefNeo4jRepository;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.RecipeUtilityFunctions;
import it.unipi.MySmartRecipeBook.utils.conversionFunctions.ChefUtilityFunctions;
import it.unipi.MySmartRecipeBook.event.Task;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    private final ChefNeo4jRepository chefNeo4jRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChefUtilityFunctions chefConvertions;
    private final RecipeUtilityFunctions recipeConvertions;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeMongoRepository;
    private final LowLoadManager lowLoadManager;
    private final IngredientService ingredientService;
    private final MongoTemplate mongoTemplate;

    public ChefService(ChefRepository chefRepository, ChefUtilityFunctions chefConvertions,
                       RecipeUtilityFunctions recipeConvertions, PasswordEncoder passwordEncoder,
                       AdminRepository adminRepository, RecipeMongoRepository recipeMongoRepository,
                       LowLoadManager lowLoadManager, IngredientService ingredientService,
                       MongoTemplate mongoTemplate, ChefNeo4jRepository chefNeo4jRepository) {
        this.chefRepository = chefRepository;
        this.chefConvertions = chefConvertions;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.recipeMongoRepository = recipeMongoRepository;
        this.lowLoadManager = lowLoadManager;
        this.ingredientService = ingredientService;
        this.recipeConvertions = recipeConvertions;
        this.mongoTemplate = mongoTemplate;
        this.chefNeo4jRepository = chefNeo4jRepository;
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

        if(!chefRepository.existsById(authChef.getId())){
            throw new NoSuchElementException("Chef not found");
        }

        Query query = new Query(Criteria.where("id").is(authChef.getId()));

        Update update = new Update();
        if (dto.getEmail() != null && StringUtils.hasText(dto.getEmail()))
            update.set("email", dto.getEmail());

        if (dto.getPassword() != null && StringUtils.hasText(dto.getPassword()))
            update.set("password", passwordEncoder.encode(dto.getPassword()));

        if (dto.getBirthdate() != null)
            update.set("birthdate", dto.getBirthdate());

        

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        Chef chef = mongoTemplate.findAndModify(query, update, options, Chef.class);

        return chefConvertions.chefToChefInfo(chef);
    }

    /**
     * Delete chef's profile
     * @param chefId Gets the chef's id in order to delete his/her profile and then the low load manager handles
     *               the deletion of the chef once the load of the cpu is lower than 30%
     * @throws NoSuchElementException if the chef doesn't exist
     */
    @Transactional
    public void deleteChef(String chefId) {

        Chef chef = chefRepository.findByUsername(chefId)
                .orElseThrow(() -> new NoSuchElementException("Chef not found"));


        recipeMongoRepository.deleteAllByChefId(chefId);
        chefRepository.delete(chef);

        lowLoadManager.addTask(Task.TaskType.DELETE_CHEF_RECIPE, chefId);
    }

    /**
     * Function called by the chef to write a new recipe: the recipe is on a waiting list at the
     * beginning, it will wait for admin's approval.
     *
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
            String ingredientQuantity = ingredient.getQuantity();

            if(!ingredientService.isValidIngredient(ingredientName)){
                throw new IllegalArgumentException("'" + ingredientName + "': invalid ingredient");
            }
            else if(!ingredient.isValidQuantity()){
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
        AdminPendingRecipe savedRecipe = recipeConvertions.createBaseRecipe(recipeDTO, chefDTO);

        if(admin.getRecipesToApprove() != null){
            for(AdminPendingRecipe recipe : admin.getRecipesToApprove()){
                if(recipe.getTitle().equals(recipeDTO.getTitle())){
                    throw new IllegalArgumentException("Recipe already waiting to be approved");
                }
            }
        }

        adminRepository.addRecipeToApprovals(admin.getId(), savedRecipe);

        PendingRecipe chefRecipe = recipeConvertions.recipeToChefRecipe(savedRecipe);
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

        if(recipeMongoRepository.deleteRecipeById(recipeId) == 0){
            throw new NoSuchElementException("Recipe not found");
        }

        boolean findRecipe = false;
        for (ChefRecipeSummary recipe : newRecipes) {
            if (recipe.getId().equals(recipeId)) {

                chef.setTotalSaves(chef.getTotalSaves() - recipe.getNumSaves());
                newRecipes.remove(recipe);

                if(chef.getOldRecipes() != null){
                    OldRecipe oldRecipe = chef.getOldRecipes().remove(0);
                    Optional<RecipeMongo> recipeMongo = recipeMongoRepository.findById(oldRecipe.getId());
                    ChefRecipeSummary reducedRecipe = recipeConvertions.recipeToChefRecipe(recipeMongo.get());
                    chef.getNewRecipes().add(reducedRecipe);
                }

                findRecipe = true;
                break;
            }
        }

        if(findRecipe == false){
            for(OldRecipe oldRecipe : chef.getOldRecipes()){
                if(oldRecipe.getId().equals(recipeId)){
                    chef.setTotalSaves(chef.getTotalSaves() - oldRecipe.getNumSaves());
                    chef.getOldRecipes().remove(oldRecipe);
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

        //ObjectId chefObjectId = new ObjectId(chef.getId());
        boolean recipeFound = chefRepository.removeRecipeFromWaiting(chef.getId(), recipeId) > 0;

        if(recipeFound){

            Admin admin = adminRepository.findByUsername("admin");
            if(admin == null){
                throw new NoSuchElementException("Admin not found");
            }

            if(admin.getRecipesToApprove() == null){
                throw new NoSuchElementException("No recipes waiting to be approved");
            }

            adminRepository.removeRecipeFromApprovals(admin.getId(), recipeId);
        }
    }

    /**
     * Function to show the total recipes to a chef
     * @param pageNumber Number of the page, each page has 5 recipes
     * @return the recipe's details
     *
     */
    public SliceRecipeDTO showRecipes (int pageNumber){

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
            hasPrevious = pageNumber == 1 ? false :  true;
        }

        else{

            List<OldRecipe> oldRecipes = chef.getOldRecipes().subList(start, end);
            List<String> ids = oldRecipes.stream().map(OldRecipe::getId).toList();
            List<RecipeMongo> recipes = recipeMongoRepository.findByIdIn(ids);
            content = recipeConvertions.MongoListToChefPreview(recipes);
        }

        boolean hasNext = (chef.getTotalRecipes() > end) ? true : false;
        return  new SliceRecipeDTO<>(content, hasNext, hasPrevious);
    }

    /**
     * Method to show the most popular recipes from a chef
     * @param pageNumber - paging
     * @return - paging with the list of recipes
     */
    public SliceRecipeDTO showPopularRecipes(int pageNumber){

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

        boolean hasPrevious = pageNumber == 1 ? false : true;
        boolean hasNext = (chef.getPopularRecipes().size() > end) ? true : false;

        List<ChefRecipeSummary> chefList = chef.getPopularRecipes().subList(start, end);
        List<ChefPreviewRecipeDTO> previewList = recipeConvertions.ChefListToSummaryList(chefList);
        return new SliceRecipeDTO<>(previewList, hasNext, hasPrevious);
    }

    /**
     * Method to show a chef his/her pending recipes
     * @param pageNumber - paging
     * @return the page with the list of pending recipes
     */
    public SliceRecipeDTO showPendingRecipes(int pageNumber) {

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
     * Ranking with top 3 chefs
     * @return the top chef for each category in the application
     */
    public List<TopChefDTO> getTopChef() {
        return chefNeo4jRepository.findTop3ChefsByCategory(CATEGORIES);
    }



}
