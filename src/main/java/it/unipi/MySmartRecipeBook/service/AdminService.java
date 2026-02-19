package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.model.Admin;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.AdminRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import it.unipi.MySmartRecipeBook.repository.AdminRepository;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.RecipeConvertions;
import it.unipi.MySmartRecipeBook.utils.ChefConvertions;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final RecipeConvertions recipeConvertions;
    private final ChefRepository chefRepository;
    private final AdminRepository adminRepository;
    private final RecipeMongoRepository recipeRepository;

    public AdminService(RecipeConvertions recipeConvertions, ChefRepository chefRepository,
                        AdminRepository adminRepository, RecipeMongoRepository recipeRepository) {
        this.recipeConvertions = recipeConvertions;
        this.chefRepository = chefRepository;
        this.adminRepository = adminRepository;
        this.recipeRepository = recipeRepository;
    }


    /*------------------- Approve a pending recipe  --------------------*/

    //@Transactional
    public void saveRecipe(String recipeId) {

        /* Get the admin information from the logged user */
        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));


        /* Get the indicated recipe from the admin list of recipes waiting to be approved */
        List<AdminRecipe> recipesToApprove = admin.getRecipesToApprove();

        if(recipesToApprove == null){
            throw new RuntimeException("No recipe has to be approved");
        }

        AdminRecipe recipeApproved = null;
        for(AdminRecipe recipe : recipesToApprove){
            if(recipe.getId().equals(recipeId)){
                recipeApproved = recipe;
                recipesToApprove.remove(recipeApproved);
                break;
            }
        }

        if(recipeApproved == null){
            throw new RuntimeException("Recipe not found");
        }

        /* When the admin approves a recipe we have to:
            - Update the informations in the chef collection (in particular we have to remove the recipe from the list of
            the ones waiting to be confirmed and insert it among the ones approved
            - Save the admin updated informations in the chef collection
            - Add the recipe to the recipe collection
         */

        RecipeMongo recipe = recipeConvertions.adminToMongoRecipe(recipeApproved);
        RecipeMongo saved_recipe = recipeRepository.save(recipe);
        String mongoId = saved_recipe.getId();

        addToChefRecipes(recipeApproved, mongoId);
        adminRepository.save(admin);


    }


    /* Function invoked by saveRecipe to update the informations about the approved recipe in the chef collection */

    private void addToChefRecipes(AdminRecipe recipe, String mongoId) {

        String chefId = recipe.getChef().getMongoId();
        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));


        /* In questo caso non c'è nessun controllo sul fatto che la ricetta effettivamente fosse presente tra quelle da confermare*/
        /* We remove the recipe from the list of the ones to confirm */
        List<ChefRecipe> recipesToConfirm = chef.getRecipesToConfirm();

        for (ChefRecipe singleRecipe : recipesToConfirm){
            if(singleRecipe.getId().equals(recipe.getId())){
                recipesToConfirm.remove(singleRecipe);
                break;
            }
        }

        /* Inserts the approved recipe into the 'newRecipes' list, maintaining the order by creation date.
        If the list reaches capacity, the oldest recipe is moved to the 'oldRecipes' list. */

        if(chef.getNewRecipes() == null){
            chef.setNewRecipes(new ArrayList<>());
        }
        else if (chef.getNewRecipes().size() == 5) {
            chef.getNewRecipes().remove(4);
        }

        ChefRecipe full_recipe = recipeConvertions.adminToChefRecipe(recipe);
        full_recipe.setId(mongoId);
        chef.getNewRecipes().add(0, full_recipe);
        chef.setTotalRecipes(chef.getTotalRecipes() + 1);
        chefRepository.save(chef);
    }

    /*
    Con aggiunta in un secondo momento
    private RecipeNeo4j createRecipeNeo4j(CreateRecipeDTO dto){
    */


    /*------------------- Discard a pending recipe  --------------------*/

    public void discardRecipe(String recipeId) {

        /* Get the admin information from the logged user */
        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));


        /* Delete the indicated recipe from the admin list of recipes waiting to be approved */
        List<AdminRecipe> recipesToApprove = admin.getRecipesToApprove();

        if(recipesToApprove == null){
            throw new RuntimeException("No recipe has to be approved");
        }

        String chefId = null;
        for(AdminRecipe recipe : recipesToApprove){
            if(recipe.getId().equals(recipeId)){
                chefId = recipe.getChef().getMongoId();
                recipesToApprove.remove(recipe);
                break;
            }
        }

        /* Delete the indicated recipe from the chef list of recipes waiting to be confirmed */
        if(chefId == null){
            throw new RuntimeException("Recipe not found");
        }

        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));


        /* In questo caso non c'è nessun controllo sul fatto che la ricetta effettivamente fosse presente tra quelle da confermare*/
        /* We remove the recipe from the list of the ones to confirm */
        List<ChefRecipe> recipesToConfirm = chef.getRecipesToConfirm();

        for (ChefRecipe singleRecipe : recipesToConfirm){
            if(singleRecipe.getId().equals(recipeId)){
                recipesToConfirm.remove(singleRecipe);
                break;
            }
        }

        adminRepository.save(admin);
        chefRepository.save(chef);
    }

    public void approveChef(String chefId) {
        /* Get the admin information from the logged user */
        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<Chef> chefToApprove = admin.getChefToApprove();
        Chef chef = null;
        //control if the chef we are approving is in the list
        for ( Chef newChef :  chefToApprove ) {
            if( newChef.getId().equals(chefId)) {
                System.out.println("Controllo chef in corso");
                chefToApprove.remove(newChef);
                chef = newChef;
                break;
            }

        }
        if (chef == null) {
            throw new RuntimeException("There are no chefs to approve");
        }

        chefRepository.save(chef);
    }

    public void declineChef (String chefId) {
        UserPrincipal logged_admin = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Admin admin = adminRepository.findById(logged_admin.getId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<Chef> chefToApprove = admin.getChefToApprove();
        Chef chef = null;
        //control if the chef we are approving is in the list
        for ( Chef newChef :  chefToApprove ) {
            if( newChef.getId().equals(chefId)) {
                System.out.println("Controllo chef in corso");
                chefToApprove.remove(newChef);
                chef = newChef;
                break;
            }

        }
        if (chef == null) {
            throw new RuntimeException("There are no chefs to approve");
        }
    }

}


