package it.unipi.MySmartRecipeBook.model.Mongo.users;

import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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


    @NotBlank(message = "Username is required")
    @Size(max = 20)
    private String username;

    @Field("new_recipes")
    private List<ChefRecipeSummary> newRecipes;

    @Field("old_recipes")
    private List<String> oldRecipes;

    @Field("recipes_to_confirm")
    private List<PendingRecipe> recipesToConfirm;

    @Field("popular_recipes")
    private List<ChefRecipeSummary> popularRecipes;

    @Field("tot_saves")
    private Integer totalSaves;

    @Field("tot_recipes")
    private Integer totalRecipes;

}