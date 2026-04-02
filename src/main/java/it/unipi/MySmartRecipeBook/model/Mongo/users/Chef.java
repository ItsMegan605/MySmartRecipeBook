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


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chefs")

public class  Chef extends RegisteredUser {

    @Field("reg_date")
    @Past
    private LocalDate registrationDate;

    @Field("new_recipes")
    private List<ChefRecipeSummary> newRecipes;

    @Field("old_recipes")
    private List<OldRecipe> oldRecipes;

    @Field("recipes_to_confirm")
    private List<ChefPendingRecipe> recipesToConfirm;

    @Field("popular_recipes")
    private List<ChefRecipeSummary> popularRecipes;

    @Field("tot_saves")
    private Integer totalSaves;

    @Field("tot_recipes")
    private Integer totalRecipes;

}

