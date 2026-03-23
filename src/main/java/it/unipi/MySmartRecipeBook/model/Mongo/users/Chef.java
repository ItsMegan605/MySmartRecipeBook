package it.unipi.MySmartRecipeBook.model.Mongo.users;

import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefPendingRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.recipes.ChefRecipeSummary;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chefs")

public class Chef extends RegisteredUser {

    @Field("reg_date")
    @Past
    private LocalDate registrationDate;

    @Field("new_recipes")
    private List<ChefRecipeSummary> newRecipes;

    @Field("top_recipes")
    private List<ChefRecipeSummary> topRecipes;

    @Field("old_recipes")
    private List<String> oldRecipes;

    @Field("recipes_to_confirm")
    private List<ChefPendingRecipe> recipesToConfirm;

    @Field("tot_saves")
    private Integer totalSaves;

    @Field("tot_recipes")
    private Integer totalRecipes;

}

