package it.unipi.MySmartRecipeBook.service;

import static it.unipi.MySmartRecipeBook.utils.parameters.Categories.*;
import it.unipi.MySmartRecipeBook.dto.InfoToDeleteDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.FoodiePreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.SliceRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.UserPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.security.UserPrincipal;
import it.unipi.MySmartRecipeBook.utils.parameters.Task;
import it.unipi.MySmartRecipeBook.repository.Mongo.FoodieRepository;
import it.unipi.MySmartRecipeBook.repository.Mongo.RecipeMongoRepository;
import it.unipi.MySmartRecipeBook.utils.convertionFunctions.FoodieUtilityFunctions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FoodieService {

    @Value("${app.recipe.pag-size-foodie:5}")
    private int pageSizeFoodie;

    private final FoodieRepository foodieRepository;
    private final RecipeMongoRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FoodieUtilityFunctions usersConvertions;
    private final LowLoadManager lowLoadManager;
    private final MongoTemplate mongoTemplate;

    public FoodieService(FoodieRepository foodieRepository, RecipeMongoRepository recipeRepository,
                         PasswordEncoder passwordEncoder, FoodieUtilityFunctions usersConvertions,
                         LowLoadManager lowLoadManager, MongoTemplate mongoTemplate) {
        this.foodieRepository = foodieRepository;
        this.recipeRepository = recipeRepository;
        this.passwordEncoder = passwordEncoder;
        this.usersConvertions = usersConvertions;
        this.lowLoadManager = lowLoadManager;
        this.mongoTemplate = mongoTemplate;
    }


    /*--------------- Retrieve foodie's informations ----------------*/

    public RegistedUserInfoDTO getById() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
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

    public RegistedUserInfoDTO updateFoodie(UpdateFoodieDTO dto) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!foodieRepository.existsById(authFoodie.getId())) {
            throw new RuntimeException("Foodie not found");
        }

        Query query = new Query(Criteria.where("id").is(authFoodie.getId()));

        Update update = new Update();
        if (dto.getName() != null && StringUtils.hasText(dto.getName()))
            update.set("name", dto.getName());

        if (dto.getSurname() != null && StringUtils.hasText(dto.getSurname()))
            update.set("surname", dto.getSurname());

        if (dto.getEmail() != null && StringUtils.hasText(dto.getEmail()))
            update.set("email", dto.getEmail());

        /* Supponiamo che se si è loggato può cambiare la password senza fare ulteriori controlli? */
        if (dto.getPassword() != null && StringUtils.hasText(dto.getPassword()))
            update.set("password", passwordEncoder.encode(dto.getPassword()));

        if (dto.getBirthdate() != null)
            update.set("birthdate", dto.getBirthdate());

        // c'è da fare il controllo che abbia almeno 10 anni

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        Foodie foodie = mongoTemplate.findAndModify(query, update, options, Foodie.class);

        // Ritorniamo le informazioni aggiornate che verranno mostrate nell'area personale
        return usersConvertions.entityToFoodieDTO(foodie);
    }


    /*----------------- Delete foodie's Profile ------------------*/

    @Transactional
    public void deleteFoodie() {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        List<String> recipesId = new ArrayList<>();
        List<String> chefsId = new ArrayList<>();

        if (foodie.getSavedRecipes() != null) {
            for (FoodieRecipeSummary recipe : foodie.getSavedRecipes()) {
                recipesId.add(recipe.getId());
                chefsId.add(recipe.getChef().getId());
            }
        }

        Map<String, Long> chefDecrements = chefsId.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        InfoToDeleteDTO infoFoodie = new InfoToDeleteDTO(recipesId, chefDecrements);
        lowLoadManager.addTask(Task.TaskType.SET_COUNTERS_FOODIE_DELETE, infoFoodie);

        foodieRepository.delete(foodie);
    }


    /*------------ Add a recipe to foodie's favourites  -------------*/

    @Transactional
    public void saveRecipe(String foodieId, String recipeId) {

        if (!foodieRepository.existsById(foodieId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid foodie");
        }

        /* We retrieve all the recipe information from the recipe collection*/
        RecipeMongo recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe to save not found"));

        // All'interno di questa entità viene memorizzata la data di salvataggio
        FoodieRecipeSummary fullRecipe = usersConvertions.entityToReducedRecipe(recipe);

        foodieRepository.addRecipeToFavourites(foodieId, recipeId, fullRecipe);
        lowLoadManager.addTask(Task.TaskType.SET_COUNTERS_ADD_FAVOURITE, recipeId, recipe.getChef().getId());
    }


    /*------------ Remove a recipe from foodie's favourites  -------------*/
    // VA FATTA PER FORZA CON VERSIONE PERCHè SE USIAMO LA LISTA "AGGIORNATA", NEL MENTRE POTREBBE ESSERCI STATO UN ALTRO
    // THREAD CHE HA MODIFICATO I PREFERITI E ANDIAMO A SOVRASCRIVERLA
    @Transactional
    public void removeSavedRecipe(String recipeId) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if (foodie.getSavedRecipes() == null) {
            throw new RuntimeException("Recipe not found for the specified foodie");
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
        }
    }


    /*------------ Show foodie's favourites recipes -------------*/

    public SliceRecipeDTO getRecipeByCategory(String category, int numPage) {

        UserPrincipal authFoodie = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Foodie foodie = foodieRepository.findById(authFoodie.getId())
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if (foodie.getSavedRecipes() == null) {
            throw new RuntimeException("Recipe not found for the specified foodie");
        }

        if (numPage <= 0 || !FOODIE_FILTERS.contains(category)) {
            throw new RuntimeException("Invalid parameters");
        }

        /*if (numPage == 1 && category.equals("saving-date")) {
            boolean hasNext = (foodie.getSavedRecipes().size() < 5) ? false : true;

            List<UserPreviewRecipeDTO> content = usersConvertions.foodieSummaryToUserPreview(foodie.getSavedRecipes());
            return new SliceRecipeDTO(content, hasNext, false);
        }*/

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
            throw new RuntimeException("Invalid page number");
        }

        recipesPreview.sort(Comparator.comparing(FoodieRecipeSummary::getSavingDate).reversed());
        int end = Math.min(start + pageSizeFoodie, recipesPreview.size());

        boolean hasNext = recipesPreview.size() > numPage * pageSizeFoodie;
        boolean hasPrevious = numPage > 1;
        List<FoodieRecipeSummary> recipes = recipesPreview.subList(start, end);

        List<FoodiePreviewRecipeDTO> content = usersConvertions.foodieSummaryToUserPreview(recipes);
        return new SliceRecipeDTO(content, hasNext, hasPrevious);

    }
}