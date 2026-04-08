package it.unipi.MySmartRecipeBook.utils.convertionFunctions;

import it.unipi.MySmartRecipeBook.dto.IngredientDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.CreateRecipeDTO;
import it.unipi.MySmartRecipeBook.dto.users.ChefInfoDTO;
import it.unipi.MySmartRecipeBook.dto.users.PendingChefDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegistedUserInfoDTO;
import it.unipi.MySmartRecipeBook.dto.recipe.ChefPreviewRecipeDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.RecipeIngredient;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefPendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
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
 *
 */
@Component
public class ChefUtilityFunctions {

    private final PasswordEncoder passwordEncoder;

    public ChefUtilityFunctions(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    // Nel momento in cui un utente compila il form per la registrazione, affinchè possa essere effettivamente registrato
    // dobbiamo criptare la password e aggiungere la data di registrazione

    /**
     *
     * @param dto
     * @return
     */
    public PendingChef createChefEntity (RegistedUserDTO dto){

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


    // Prendiamo le informazioni da mostrare nell'area personale dello chef a partire dall'entità Chef in MongoDB
    // (in particolare non mostriamo la password per questioni di sicurezza)

    /**
     *
     * @param chef
     * @return
     */
    public RegistedUserInfoDTO chefToChefInfo(Chef chef){

        return new RegistedUserInfoDTO(
                chef.getUsername(),
                chef.getName(),
                chef.getSurname(),
                chef.getEmail(),
                chef.getBirthDate()
        );
    }

    // Ricetta che viene creata nel momento in cui uno chef fa submit del form compilato con tutte le informazioni
    // necessarie per l'inserimento di una ricetta

    /**
     *
     * @param dto
     * @param chefDTO
     * @return
     */
    public PendingRecipe createBaseRecipe (CreateRecipeDTO dto, ChefInfoDTO chefDTO){

        PendingRecipe recipe = new PendingRecipe();
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


    /* Function to create a ChefRecipe from a BaseRecipe. To be more specific, we have an
    additional field "NumSaves" that counts how many foodies has saved that specific recipe.
    In addition, we remove all the informations about the chef that would have been redundant.
    */

    /**
     *
     * @param recipe
     * @return
     */
    public ChefPendingRecipe recipeToChefRecipe (PendingRecipe recipe){

        ChefPendingRecipe full_recipe = new ChefPendingRecipe();
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


    /* Function to create a ChefPreviewRecipeDTO from an AdminRecipe*/

    /**
     *
     * @param recipe
     * @return
     */
    public ChefPreviewRecipeDTO baseToChefDTO(PendingRecipe recipe){

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
     *
     * @param recipesToConvert
     * @return
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
     *
     * @param recipesList
     * @return
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
     *
     * @param recipesToConvert
     * @return
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
     *
     * @param targetChef
     * @param chef
     * @return
     */
    public boolean chefAlreadyInserted(PendingChef targetChef, PendingChef chef) {

        boolean sameRequest = targetChef.getName().equals(chef.getName()) &&
                targetChef.getSurname().equals(chef.getSurname()) &&
                targetChef.getBirthdate().equals(chef.getBirthdate());

        boolean sameUsername = targetChef.getUsername().equals(chef.getUsername());
        return sameRequest || sameUsername;
    }

    /**
     *
     * @param chef
     * @return
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
     *
     * @param recipeMongo
     * @return
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

    public List<PendingChefDTO> PendingChefListToDTO(List<PendingChef> chefs) {
        List<PendingChefDTO> result = new ArrayList<>();
        for (PendingChef chef : chefs) {
            result.add(new PendingChefDTO(chef.getUsername(), chef.getName(), chef.getSurname()));
        }
        return result;
    }

    public List<ChefPreviewRecipeDTO> PendingChefListToChefPreview(List<ChefPendingRecipe> recipes) {
        List<ChefPreviewRecipeDTO> result = new ArrayList<>();
        for (ChefPendingRecipe recipe : recipes) {
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
