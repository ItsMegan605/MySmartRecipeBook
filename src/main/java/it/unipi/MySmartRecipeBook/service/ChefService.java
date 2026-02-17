package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateChefDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Admin;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.AdminRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.ReducedChef;
import it.unipi.MySmartRecipeBook.repository.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;

import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.ChefConvertions;
import it.unipi.MySmartRecipeBook.utils.RecipeConvertions;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChefService {

    private final ChefRepository chefRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChefConvertions chefConvertions;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeMongoRepository;
    private final RecipeConvertions recipeConvertions;

    public ChefService(ChefRepository chefRepository, ChefConvertions chefConvertions,
                       PasswordEncoder passwordEncoder, AdminRepository adminRepository, RecipeMongoRepository recipeMongoRepository, RecipeConvertions recipeConvertions) {
        this.chefRepository = chefRepository;
        this.chefConvertions = chefConvertions;
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
        this.recipeMongoRepository = recipeMongoRepository;
        this.recipeConvertions = recipeConvertions;
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

        chefRepository.delete(chef);
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
        chef.setMongoId(chef1.getId());
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

        boolean recipeFound = false;


        /* We check if the recipe we want to delete is in the newRecipes and, if needed, we move an old recipe to the
        NewRecipe list */
        for (ChefRecipe recipe : newRecipes) {
            if (recipe.getId().equals(recipeId)) {
                newRecipes.remove(recipe);
                recipeFound = true;

                List<ChefRecipeSummary> oldRecipes = chef.getOldRecipes();
                if(oldRecipes != null) {

                    String oldRecipeId = oldRecipes.get(0).getMongoId();
                    RecipeMongo oldRecipe = recipeMongoRepository.findById(oldRecipeId)
                            .orElseThrow(() -> new RuntimeException("Recipe not found"));

                    ChefRecipe recipeToInsert = recipeConvertions.entityToChefRecipe(oldRecipe);
                    newRecipes.add(recipeToInsert);
                    chef.getOldRecipes().remove(chef.getOldRecipes().get(0));
                }
                break;
            }
        }

        /* If we don't have already found the recipe we are interested in, we check in the oldRecipe list */
        if (!recipeFound) {
            List<ChefRecipeSummary> oldRecipes = chef.getOldRecipes();
            for (ChefRecipeSummary recipe : oldRecipes) {
                if (recipe.getMongoId().equals(recipeId)) {
                    oldRecipes.remove(recipe);
                    break;
                }
            }
        }

        chefRepository.save(chef);
        recipeMongoRepository.deleteById(recipeId);

        /* Attenzione ... dobbiamo fare la rimozione su neo4j in differita */
    }
}
