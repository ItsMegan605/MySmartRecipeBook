package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Admin;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.AdminRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.ReducedChef;
import it.unipi.MySmartRecipeBook.model.enums.Task;
import it.unipi.MySmartRecipeBook.repository.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;

import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.ChefConvertions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChefService {

    @Value("${app.recipe.pag-size-chef:5}")
    private Integer pageSizeChef;

    private static final List<String> VALID_FILTER = List.of(
        "date",
        "popularity"
    );

    private final ChefRepository chefRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChefConvertions chefConvertions;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeMongoRepository;
    private final LowLoadManager lowLoadManager;

    public ChefService(ChefRepository chefRepository, ChefConvertions chefConvertions,
                       PasswordEncoder passwordEncoder, AdminRepository adminRepository,
                       RecipeMongoRepository recipeMongoRepository, LowLoadManager lowLoadManager) {
        this.chefRepository = chefRepository;
        this.chefConvertions = chefConvertions;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.recipeMongoRepository = recipeMongoRepository;
        this.lowLoadManager = lowLoadManager;
    }


    /*--------------- Retrieve chef's informations ----------------*/

    public ChefInfoDTO getByUsername(String username) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        return chefConvertions.chefToChefInfo(chef);
    }


    /*--------------- Change chef's informations ----------------*/
    /* This function allows a chef to change its personal information, in particolar one or more among the following
    fields:
        - Email
        - Password
        - Birthday

     We don't allow a foodie to change his/her username, name and surname for security reasons */

    public ChefInfoDTO updateChef(String username, UpdateChefDTO dto) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        if (dto.getEmail() != null)
            chef.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            chef.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getBirthdate() != null)
            chef.setBirthdate(dto.getBirthdate());

        chefRepository.save(chef);
        return chefConvertions.chefToChefInfo(chef);
    }


    /*----------------- Delete chef's profile ----------------*/

    public void deleteChef(String username) {

        Chef chef = chefRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        String chefId = chef.getId();

        recipeMongoRepository.deleteAllByChefId(chefId);
        chefRepository.delete(chef);

        lowLoadManager.addTask(Task.TaskType.DELETE_CHEF_RECIPE, chefId);

    }


    /*------------------- Add new recipe --------------------*/

    // @Transaction
    public ChefPreviewRecipeDTO createRecipe(CreateRecipeDTO dto) {

        /* We add the entire recipe to the admin list of recipes waiting to be approved */
        Admin admin = adminRepository.findByUsername("admin");

        if (admin == null) {
            throw new RuntimeException("Admin not found");
        }

        AdminRecipe savedRecipe = createAdminRecipe(dto);

        if(admin.getRecipesToApprove() == null){
            admin.setRecipesToApprove(new ArrayList<>());
        }

        for(AdminRecipe recipe : admin.getRecipesToApprove()){
            if(recipe.getTitle().equals(dto.getTitle())){
                throw new RuntimeException("Recipe already waiting to be approved");
            }
        }

        /* We want to show first the recipes that have been pending for the longest time */
        admin.getRecipesToApprove().add(savedRecipe);
        adminRepository.save(admin);


        /* We add the entire recipe to the chef list of recipes waiting to be approved by the admin*/
        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(chef1.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        ChefRecipe chefRecipe = chefConvertions.adminToChefRecipe(savedRecipe);

        if(chef.getRecipesToConfirm() == null){
            chef.setRecipesToConfirm(new ArrayList<>());
        }

        /* We want to show first the recipes that have been pending for the longest time */
        chef.getRecipesToConfirm().add(chefRecipe);
        chefRepository.save(chef);

        return chefConvertions.adminToChefDTO(savedRecipe);

    }

    private AdminRecipe createAdminRecipe (CreateRecipeDTO dto){

        AdminRecipe recipe = new AdminRecipe();
        recipe.setId(java.util.UUID.randomUUID().toString());
        recipe.setTitle(dto.getTitle());
        recipe.setCategory(dto.getCategory());
        recipe.setPreparation(dto.getPreparation());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setPresentation(dto.getPresentation());
        recipe.setImageURL(dto.getImageURL());
        recipe.setIngredients(dto.getIngredients());
        recipe.setCreationDate(LocalDateTime.now());

        ReducedChef chef = new ReducedChef();
        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        chef.setId(chef1.getId());
        chef.setName(chef1.getName());
        chef.setSurname(chef1.getSurname());

        recipe.setChef(chef);

        return recipe;
    }


    /*--------------- Delete a recipe  ----------------*/

    public void deleteRecipe(String recipeId) {

        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(chef1.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        List<ChefRecipe> newRecipes = chef.getNewRecipes();

        if (newRecipes == null) {
            throw new RuntimeException("No recipes found");
        }

        for (ChefRecipe recipe : newRecipes) {
            if (recipe.getId().equals(recipeId)) {
                newRecipes.remove(recipe);

                Pageable pageable = PageRequest.of(0, pageSizeChef, Sort.by("creationDate").descending());
                Slice<RecipeMongo> matchSlice = recipeMongoRepository.findByChefId(chef1.getId(), pageable);
                List<RecipeMongo> matchRecipes = matchSlice.getContent();

                List<ChefRecipe> recipesToSave = chefConvertions.MongoListToChefList(matchRecipes);
                chef.setNewRecipes(recipesToSave);
                chef.setTotalRecipes(chef.getTotalRecipes() - 1);
                chefRepository.save(chef);
                break;
            }
        }

        recipeMongoRepository.deleteById(recipeId);

        /* Attenzione ... dobbiamo fare la rimozione su neo4j in differita */
    }


    // @Transactional
    public void removeRecipe(String recipeId) {

        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(chef1.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        boolean recipeFound = false;

        if(chef.getRecipesToConfirm() == null){
            throw new RuntimeException("No recipes waiting to be confirmed");
        }

        for(ChefRecipe recipe : chef.getRecipesToConfirm()){
            if(recipe.getId().equals(recipeId)){

                recipeFound = true;
                chef.getRecipesToConfirm().remove(recipe);
                chefRepository.save(chef);
                break;
            }
        }

        if(!recipeFound){
            throw new RuntimeException("Recipe not found");
        }
        else {

            Admin admin = adminRepository.findByUsername("admin");
            if(admin == null){
                throw new RuntimeException("Admin not found");
            }

            if(admin.getRecipesToApprove() == null){
                throw new RuntimeException("No recipes waiting to be approved");
            }

            for(AdminRecipe adminRecipe : admin.getRecipesToApprove()){
                if (adminRecipe.getId().equals(recipeId)){
                    admin.getRecipesToApprove().remove(adminRecipe);
                    adminRepository.save(admin);
                    return;
                }
            }
        }
    }



    /*------------------- Show recipe --------------------*/
    // @Transactional
    public Slice<ChefPreviewRecipeDTO> showRecipes(String filter, Integer pageNumber) {

        UserPrincipal chef1 = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Chef chef = chefRepository.findById(chef1.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        if(pageNumber <= 0 || !VALID_FILTER.contains(filter)){
            throw new RuntimeException("Invalid parameters");
        }


        Pageable pageable = null;
        if(filter.equals("date")){
            if(pageNumber == 1){

                if (chef.getNewRecipes() == null || chef.getNewRecipes().isEmpty()) {
                    return new SliceImpl<>(new ArrayList<>(), PageRequest.of(pageNumber - 1, pageSizeChef), false);
                }

                List<ChefPreviewRecipeDTO> content = chefConvertions.ChefListToSummaryList(chef.getNewRecipes());
                pageable = PageRequest.of(pageNumber - 1, pageSizeChef);
                boolean hasNext = (chef.getTotalRecipes() > pageSizeChef) ? true : false;

                return  new SliceImpl<>(content, pageable, hasNext);
            }
            else{
                pageable = PageRequest.of(pageNumber - 1, pageSizeChef,
                        Sort.by("creationDate").descending());
            }
        }
        else if(filter.equals("popularity")){
            pageable = PageRequest.of(pageNumber - 1, pageSizeChef,
                    Sort.by("numSaves").descending());
        }

        Slice<RecipeMongo> recipesPage = recipeMongoRepository.findByChefId(chef.getId(), pageable);
        List<ChefPreviewRecipeDTO> content = chefConvertions.MongoListToChefPreview(recipesPage.getContent());
        boolean hasNext = (chef.getTotalRecipes() > pageSizeChef*pageNumber) ? true : false;

        return  new SliceImpl<>(content, pageable, hasNext);
    }

}
