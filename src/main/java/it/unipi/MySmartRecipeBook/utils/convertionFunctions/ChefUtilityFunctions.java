package it.unipi.MySmartRecipeBook.utils.convertionFunctions;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.PendingChefDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.AdminPendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.RecipeMongo;
import it.unipi.MySmartRecipeBook.model.Mongo.users.PendingChef;
import it.unipi.MySmartRecipeBook.model.Mongo.users.ReducedChef;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for Chef-related entity and DTO conversions.
 */
@Component
public class ChefUtilityFunctions {

    private final PasswordEncoder passwordEncoder;

    public ChefUtilityFunctions(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    //when a user registers, in order to be registered we need to encypt the password and
    //update the registration date

    /**
     * Converts a registration DTO into a PendingChef .
     * @param dto the registration data
     * @return the PendingChef entity
     */
    public PendingChef createChefEntity (RegisteredUserDTO dto){

        PendingChef chef = new PendingChef();
        chef.setUsername(dto.getUsername());
        chef.setName(dto.getName());
        chef.setSurname(dto.getSurname());

        chef.setEmail(dto.getEmail());
        chef.setPassword(passwordEncoder.encode(dto.getPassword()));

        chef.setBirthdate(dto.getBirthdate());
        chef.setRegistrationDate(LocalDate.now());

        return chef;
    }


    /**
     * Converts a Chef entity to a DTO for the profile.
     * password is not shown for security
     * @param chef the Chef entity
     * @return the registered user info DTO
     */
    public RegisteredUserInfoDTO chefToChefInfo(Chef chef){

        return new RegisteredUserInfoDTO(
                chef.getUsername(),
                chef.getName(),
                chef.getSurname(),
                chef.getEmail(),
                chef.getBirthDate()
        );
    }

    /**
     * Creates a AdminPendingRecipe from a creation DTO and Chef info.
     * @param dto the recipe creation data
     * @param chefDTO the chef information
     * @return the AdminPendingRecipe entity
     */
    public AdminPendingRecipe createBaseRecipe (CreateRecipeDTO dto, ChefInfoDTO chefDTO){

        AdminPendingRecipe recipe = new AdminPendingRecipe();
        recipe.setId(java.util.UUID.randomUUID().toString());
        recipe.setTitle(dto.getTitle());
        recipe.setCategory(dto.getCategory());
        recipe.setPreparation(dto.getPreparation());
        recipe.setPrepTime(dto.getPrepTime());
        recipe.setDifficulty(dto.getDifficulty());
        recipe.setPresentation(dto.getPresentation());
        recipe.setImageURL(dto.getImageURL());

        List<RecipeIngredient> ingredients = new ArrayList<>();
        for(IngredientDTO ingredientDTO : dto.getIngredients()){
            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setName(ingredientDTO.getName());
            ingredient.setQuantity(ingredientDTO.getQuantity());
            ingredients.add(ingredient);
        }
        recipe.setIngredients(ingredients);
        recipe.setCreationDate(LocalDateTime.now());

        ReducedChef chef = new ReducedChef();
        chef.setId(chefDTO.getId());
        chef.setName(chefDTO.getName());
        chef.setSurname(chefDTO.getSurname());

        recipe.setChef(chef);

        return recipe;
    }


    /**
     * Converts a AdminPendingRecipe into a PendingRecipe: extra NumSaves field.
     * It removes redundant chef information.
     * @param recipe the pending recipe
     * @return the chef pending recipe summary
     */
    public PendingRecipe recipeToChefRecipe (AdminPendingRecipe recipe){

        PendingRecipe full_recipe = new PendingRecipe();
        full_recipe.setId(recipe.getId());
        full_recipe.setTitle(recipe.getTitle());
        full_recipe.setPresentation(recipe.getPresentation());
        full_recipe.setCategory(recipe.getCategory());
        full_recipe.setPrepTime(recipe.getPrepTime());
        full_recipe.setPreparation(recipe.getPreparation());
        full_recipe.setDifficulty(recipe.getDifficulty());
        full_recipe.setImageURL(recipe.getImageURL());
        full_recipe.setIngredients(recipe.getIngredients());
        full_recipe.setCreationDate(recipe.getCreationDate());

        return full_recipe;
    }


    /**
     * Converts a AdminPendingRecipe into a ChefPreviewRecipeDTO using a temporary ID.
     * @param recipe the pending recipe
     * @return the preview DTO
     */
    public ChefPreviewRecipeDTO baseToChefDTO(AdminPendingRecipe recipe){

        ChefPreviewRecipeDTO recipeDTO = new ChefPreviewRecipeDTO();

        /* In this case the id is the temporary one (not the one generated by Mongo) since the recipe has not
        been inserted in the recipes collection yet */
        recipeDTO.setId(recipe.getId());
        recipeDTO.setTitle(recipe.getTitle());
        recipeDTO.setImageURL(recipe.getImageURL());
        recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());

        return recipeDTO;
    }

    /**
     * Converts a list of RecipeMongo entities to a list of ChefRecipeSummary DTOs.
     * @param recipesToConvert the list of mongo recipes
     * @return the list of summaries
     */
    public List<ChefRecipeSummary> MongoListToChefListSummary(List<RecipeMongo> recipesToConvert) {

        List<ChefRecipeSummary> chefRecipes = new ArrayList<>();

        for(RecipeMongo recipeMongo : recipesToConvert){
            ChefRecipeSummary recipe = new ChefRecipeSummary();

            recipe.setId(recipeMongo.getId());
            recipe.setTitle(recipeMongo.getTitle());
            recipe.setImageURL(recipeMongo.getImageURL());
            recipe.setCreationDate(recipeMongo.getCreationDate());
            recipe.setNumSaves(recipeMongo.getNumSaves());

            chefRecipes.add(recipe);
        }

        return chefRecipes;
    }

    /**
     * Converts a list of ChefRecipeSummary entities to a list of ChefPreviewRecipeDTOs.
     * @param recipesList the list of recipe summaries
     * @return the list of preview DTOs
     */
    public List<ChefPreviewRecipeDTO> ChefListToSummaryList(List<ChefRecipeSummary> recipesList) {

        List<ChefPreviewRecipeDTO> chefPreviewList = new ArrayList<>();
        for(ChefRecipeSummary recipe : recipesList){
            ChefPreviewRecipeDTO recipeDTO = new ChefPreviewRecipeDTO();

            recipeDTO.setId(recipe.getId());
            recipeDTO.setTitle(recipe.getTitle());
            recipeDTO.setImageURL(recipe.getImageURL());
            recipeDTO.setCreationDate(recipe.getCreationDate().toLocalDate());
            recipeDTO.setNumSaves(
                    recipe.getNumSaves() == null ? 0 : recipe.getNumSaves()
            );

            chefPreviewList.add(recipeDTO);
        }

        return  chefPreviewList;
    }

    /**
     * Converts a list of RecipeMongo entities directly to ChefPreviewRecipeDTOs.
     * @param recipesToConvert the list of mongo recipes
     * @return the list of preview DTOs
     */
    public List<ChefPreviewRecipeDTO> MongoListToChefPreview(List<RecipeMongo> recipesToConvert) {

        List<ChefPreviewRecipeDTO> chefRecipes = new ArrayList<>();

        for(RecipeMongo recipeMongo : recipesToConvert){
            ChefPreviewRecipeDTO recipe = new ChefPreviewRecipeDTO();

            recipe.setId(recipeMongo.getId());
            recipe.setTitle(recipeMongo.getTitle());
            recipe.setImageURL(recipeMongo.getImageURL());
            recipe.setCreationDate(recipeMongo.getCreationDate().toLocalDate());
            recipe.setNumSaves(recipeMongo.getNumSaves());
            chefRecipes.add(recipe);
        }

        return chefRecipes;
    }

    /**
     * Checks if a chef registration request already exists based on personal data or username.
     * @param targetChef the existing pending chef
     * @param chef the new pending chef
     * @return true if a duplicate exists, false otherwise
     */
    public boolean chefAlreadyInserted(PendingChef targetChef, PendingChef chef) {

        boolean sameRequest = targetChef.getName().equals(chef.getName()) &&
                targetChef.getSurname().equals(chef.getSurname()) &&
                targetChef.getBirthdate().equals(chef.getBirthdate());

        boolean sameUsername = targetChef.getUsername().equals(chef.getUsername());
        return sameRequest || sameUsername;
    }

    /**
     * Converts an approved PendingChef into a final Chef entity.
     * @param chef the pending chef
     * @return the final Chef entity
     */
    public Chef pendingChefToChef (PendingChef chef){

        Chef chefMongo = new Chef();
        chefMongo.setId(chef.getId());
        chefMongo.setUsername(chef.getUsername());
        chefMongo.setPassword(chef.getPassword());
        chefMongo.setName(chef.getName());
        chefMongo.setSurname(chef.getSurname());
        chefMongo.setEmail(chef.getEmail());
        chefMongo.setBirthDate(chef.getBirthdate());
        chefMongo.setRegistrationDate(chef.getRegistrationDate());
        return chefMongo;
    }

    /**
     * Converts a RecipeMongo entity into a ChefRecipeSummary.
     * @param recipeMongo the mongo recipe
     * @return the chef recipe summary
     */
    public ChefRecipeSummary recipeToChefRecipe (RecipeMongo recipeMongo){

        ChefRecipeSummary recipe = new ChefRecipeSummary();
        recipe.setId(recipeMongo.getId());
        recipe.setTitle(recipeMongo.getTitle());
        recipe.setImageURL(recipeMongo.getImageURL());
        recipe.setCreationDate(recipeMongo.getCreationDate());
        recipe.setNumSaves(recipeMongo.getNumSaves());

        return recipe;
    }


    /**
     * Converts a list of PendingChef entities into a list of PendingChefDTOs.
     * @param chefs the list of pending chefs
     * @return the list of pending chef DTOs
     */
    public List<PendingChefDTO> PendingChefListToDTO(List<PendingChef> chefs) {
        List<PendingChefDTO> result = new ArrayList<>();
        for (PendingChef chef : chefs) {
            result.add(new PendingChefDTO(chef.getUsername(), chef.getName(), chef.getSurname()));
        }
        return result;
    }

    /**
     * Converts a list of PendingRecipe entities into a list of ChefPreviewRecipeDTOs.
     * @param recipes the list of chef pending recipes
     * @return the list of preview DTOs
     */
    public List<ChefPreviewRecipeDTO> PendingChefListToChefPreview(List<PendingRecipe> recipes) {
        List<ChefPreviewRecipeDTO> result = new ArrayList<>();
        for (PendingRecipe recipe : recipes) {
            ChefPreviewRecipeDTO dto = new ChefPreviewRecipeDTO();
            dto.setId(recipe.getId());
            dto.setTitle(recipe.getTitle());
            dto.setImageURL(recipe.getImageURL());
            dto.setCreationDate(recipe.getCreationDate().toLocalDate());
            result.add(dto);
        }
        return result;
    }

}
