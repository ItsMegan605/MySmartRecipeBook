package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.foodie.StandardFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.foodie.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.*;
import it.unipi.MySmartRecipeBook.model.Recipe;
import it.unipi.MySmartRecipeBook.model.enums.Task;
import it.unipi.MySmartRecipeBook.repository.ChefRepository;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.UsersConvertions;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FoodieService {


    private static final List<String> VALID_CATEGORIES = List.of(
            "vegan",
            "dairy-free",
            "egg-free",
            "gluten-free",
            "main-course",
            "second-course",
            "dessert"
    );

    private static final List<String> VALID_FILTERS = List.of(
            "vegan",
            "dairy-free",
            "egg-free",
            "gluten-free",
            "main-course",
            "second-course",
            "dessert",
            "saving-date",
            "easy",
            "very hard",
            "hard",
            "average",
            "very easy"
    );

    private static final List<String> VALID_DIFFICULTIES = List.of(

            "very easy",
            "easy",
            "very hard",
            "hard",
            "average"
    );
    private final LowLoadManager lowLoadManager;

    @Value("${app.recipe.pag-size-foodie:5}")
    private Integer pageSizeFoodie;

    private final FoodieRepository foodieRepository;
    private final ChefRepository chefRepository;
    private final RecipeMongoRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersConvertions usersConvertions;

    public FoodieService(FoodieRepository foodieRepository, RecipeMongoRepository recipeRepository,
                         PasswordEncoder passwordEncoder, UsersConvertions usersConvertions,
                         ChefRepository chefRepository, LowLoadManager lowLoadManager) {
        this.foodieRepository = foodieRepository;
        this.recipeRepository = recipeRepository;
        this.passwordEncoder = passwordEncoder;
        this.usersConvertions = usersConvertions;
        this.chefRepository = chefRepository;
        this.lowLoadManager = lowLoadManager;
    }


    /*--------------- Retrieve foodie's informations ----------------*/

    public StandardFoodieDTO getByUsername(String username) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        return usersConvertions.entityToFoodieDTO(foodie);
    }


    /*--------------- Change foodie's informations ----------------*/
    /* This function allows a foodie to change its personal information, in particolar one or more among the following
    fields:
        - Name
        - Surname
        - Email
        - Password
        - Birthday

     We don't allow a foodie to change his/her username for security reasons */

    public StandardFoodieDTO updateFoodie(String username, UpdateFoodieDTO dto) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if (dto.getName() != null)
            foodie.setName(dto.getName());

        if (dto.getSurname() != null)
            foodie.setSurname(dto.getSurname());

        if (dto.getEmail() != null)
            foodie.setEmail(dto.getEmail());

        /* Supponiamo che se si è loggato può cambiare la password senza fare ulteriori controlli? */
        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            foodie.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getBirthdate() != null)
            foodie.setBirthdate(dto.getBirthdate());

        foodieRepository.save(foodie);
        return usersConvertions.entityToFoodieDTO(foodie);
    }


    /*----------------- Delete foodie's Profile ------------------*/

    public void deleteFoodie(String username){

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        List<String> recipesId = new ArrayList<>();
        List<String> chefsId = new ArrayList<>();

        if(foodie.getNewSavedRecipes() != null){
            for(FoodieRecipe recipe: foodie.getNewSavedRecipes()){
                recipesId.add(recipe.getId());
                chefsId.add(recipe.getChef().getMongoId());
            }
        }

        if(foodie.getOldSavedRecipes() != null){
            for(FoodieRecipeSummary recipe: foodie.getOldSavedRecipes()){
                recipesId.add(recipe.getId());
                chefsId.add(recipe.getChefId());
            }
        }

        Map<String, Long> chefDecrements = chefsId.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        InfoToDeleteDTO infoFoodie = new InfoToDeleteDTO(recipesId, chefDecrements);
        lowLoadManager.addTask(Task.TaskType.SET_COUNTERS_FOODIE_DELETE, infoFoodie);
        foodieRepository.delete(foodie);
    }


    /*------------ Add a recipe to foodie's favourites  -------------*/

    public void saveRecipe(String foodieId, String recipeId) {

        Foodie foodie = foodieRepository.findById(foodieId)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if(foodie.getNewSavedRecipes() != null) {
            //check if the recipe has already been saved in the last recipes
            for (FoodieRecipe recipe : foodie.getNewSavedRecipes()) {
                if (recipe.getId().equals(recipeId))
                    throw new RuntimeException("Recipe already saved");
            }
        }

        if(foodie.getOldSavedRecipes() != null) {
            //check if the recipe has already been saved in the old recipes
            for (FoodieRecipeSummary recipe : foodie.getOldSavedRecipes()) {
                if (recipe.getId().equals(recipeId))
                    throw new RuntimeException("Recipe already saved");
            }
        }

        /* We retrieve all the recipe informations from the recipe collection*/
        RecipeMongo recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe to save not found"));

        FoodieRecipe fullRecipe = usersConvertions.entityToFoodieEntity(recipe);

        /* If it is the first save, we have to create the NewSavedRecipes list */
        if (foodie.getNewSavedRecipes() == null) {
            foodie.setNewSavedRecipes(new ArrayList<>());
        }

        /* If the NewSavedRecipes list is already full we have to move the oldest recipe of the list in the OldSavedRecipe
        list and then we insert the new recipe in the NewSavedRecipe */
        if(foodie.getNewSavedRecipes().size() == 5){

            if (foodie.getOldSavedRecipes() == null) {
                foodie.setOldSavedRecipes(new ArrayList<>());
            }

            int index = foodie.getNewSavedRecipes().size() - 1;
            FoodieRecipe oldestRecipe = foodie.getNewSavedRecipes().remove(index);
            FoodieRecipeSummary reduced_old = usersConvertions.entityToReducedRecipe(oldestRecipe);

            /* We insert the element on the head of the list, in order to implement the FIFO policy */
            foodie.getOldSavedRecipes().add(0, reduced_old);
        }

        foodie.getNewSavedRecipes().add(0, fullRecipe);
        foodieRepository.save(foodie);

        recipe.setNumSaves(recipe.getNumSaves() + 1);
        recipeRepository.save(recipe);

        /* Si potrebbe fare che il secondo aggiornamento si fa con basso carico */
        // updateChefCounters(recipe.getChef().getMongoId(), recipe.getId(), 1);
    }


    // @Transactional
    private void updateChefCounters(String chefId, String recipeId, Integer increment) {

        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        if(chef.getNewRecipes() == null){
            throw new RuntimeException("Recipe not found for the specified chef");
        }

        for (ChefRecipe recipe : chef.getNewRecipes()) {
            if (recipe.getId().equals(recipeId)){
                recipe.setNumSaves(recipe.getNumSaves() + increment);
                break;
            }
        }

        chef.setTotalSaves(chef.getTotalSaves() + increment);
        chefRepository.save(chef);
    }


    public void removeSavedRecipe(String foodieId, String recipeId) {

        Foodie foodie = foodieRepository.findById(foodieId)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if(foodie.getNewSavedRecipes() ==  null){
            throw new RuntimeException("Recipe not found for the specified foodie");
        }

        FoodieRecipe recipeRemoved = null;
        for (FoodieRecipe recipe : foodie.getNewSavedRecipes()) {
            if(recipe.getId().equals(recipeId)){
                recipeRemoved = recipe;
                foodie.getNewSavedRecipes().remove(recipe);

                if(foodie.getOldSavedRecipes() != null){

                    String oldRecipeId = foodie.getOldSavedRecipes().get(0).getId();
                    RecipeMongo oldRecipe =  recipeRepository.findById(oldRecipeId)
                            .orElseThrow(() -> new RuntimeException("Recipe not found"));

                    FoodieRecipe recipeToMove = usersConvertions.entityToFoodieEntity(oldRecipe);
                    foodie.getNewSavedRecipes().add(recipeToMove);

                    foodie.getOldSavedRecipes().remove(0);
                }
                foodieRepository.save(foodie);
                break;
            }
        }

        if(recipeRemoved == null &&  foodie.getOldSavedRecipes() != null){
            for (FoodieRecipeSummary recipe : foodie.getOldSavedRecipes()) {
                if (recipe.getId().equals(recipeId)){
                    foodie.getOldSavedRecipes().remove(recipe);
                    foodieRepository.save(foodie);
                    break;
                }
            }
        }

        RecipeMongo recipeToUpdate = recipeRepository.findById(recipeId).orElseThrow(() -> new RuntimeException("Recipe not found"));
        recipeToUpdate.setNumSaves(recipeToUpdate.getNumSaves() - 1);
        recipeRepository.save(recipeToUpdate);

        /* Si potrebbe fare che il secondo aggiornamento si fa con basso carico */
        // updateChefCounters(recipe.getChef().getMongoId(), recipe.getId(), -1);
    }


    public Slice<UserPreviewRecipeDTO> getRecipeByCategory(String foodieId, String category, Integer numPage) {

        Foodie foodie = foodieRepository.findById(foodieId)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if(foodie.getNewSavedRecipes() == null){
            throw new RuntimeException("Recipe not found for the specified foodie");
        }

        if(numPage <= 0 || !VALID_FILTERS.contains(category)){
            throw new RuntimeException("Invalid parameters");
        }

        List<FoodieRecipeSummary> reducedRecipes = usersConvertions.foodieListToSummaryList(foodie.getNewSavedRecipes());
        if(numPage == 1 && category.equals("date")){
            Pageable pageable = PageRequest.of(numPage - 1, pageSizeFoodie, Sort.by("savingDate").descending() );
            boolean hasNext = (foodie.getOldSavedRecipes() == null) ? false : true;

            List<UserPreviewRecipeDTO> content = usersConvertions.foodieSummaryToUserPreview(reducedRecipes);
            return  new SliceImpl<>(content, pageable, hasNext);
        }

        List<FoodieRecipeSummary> allRecipes = new ArrayList<>(reducedRecipes);
        if(foodie.getOldSavedRecipes() != null){
            allRecipes.addAll(foodie.getOldSavedRecipes());
        }

        List<FoodieRecipeSummary> recipesPreview = new ArrayList<>();
        if(VALID_CATEGORIES.contains(category)){
            for(FoodieRecipeSummary recipe : allRecipes){
                if(recipe.getCategory().equals(category)){
                    recipesPreview.add(recipe);
                }
            }
        }
        else if(VALID_DIFFICULTIES.contains(category)){
            for(FoodieRecipeSummary recipe : allRecipes){
                if(recipe.getDifficulty().equals(category)){
                    recipesPreview.add(recipe);
                }
            }
        }
        else if (category.equals("date")) {
            recipesPreview.addAll(allRecipes);
        }

        Integer start = (numPage - 1) * pageSizeFoodie;
        if(start > recipesPreview.size()){
            throw new RuntimeException("Invalid page number");
        }

        Integer end = Math.min(start + pageSizeFoodie, recipesPreview.size());

        boolean hasNext = recipesPreview.size() > numPage*pageSizeFoodie;
        List<FoodieRecipeSummary> recipes = recipesPreview.subList(start, end);

        Pageable pageable = PageRequest.of(numPage-1, pageSizeFoodie, Sort.by("savingDate").descending());
        List<UserPreviewRecipeDTO> content = usersConvertions.foodieSummaryToUserPreview(recipes);
        return  new SliceImpl<>(content, pageable, hasNext);

    }

}
