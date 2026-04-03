package it.unipi.MySmartRecipeBook.model.Mongo.users;

import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefPendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.OldRecipe;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a chef user in the system.

 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chefs")

public class Chef extends RegisteredUser {

    /**
     * Registration date of the chef.
     * Must be a past date.
     */
    @Field("reg_date")
    @Past
    private LocalDate registrationDate;

    /**
     * List of recently created recipes.
     */
    @Field("new_recipes")
    private List<ChefRecipeSummary> newRecipes;

    /**
     * List of older recipes.
     */
    @Field("old_recipes")
    private List<OldRecipe> oldRecipes;

    /**
     * List of recipes pending confirmation.
     */
    @Field("recipes_to_confirm")
    private List<ChefPendingRecipe> recipesToConfirm;

    /**
     * List of most popular recipes.
     */
    @Field("popular_recipes")
    private List<ChefRecipeSummary> popularRecipes;

    /**
     * Total number of saves across all recipes.
     */
    @Field("tot_saves")
    private Integer totalSaves;

    /**
     * Total number of recipes created by the chef.
     */
    @Field("tot_recipes")
    private Integer totalRecipes;

}