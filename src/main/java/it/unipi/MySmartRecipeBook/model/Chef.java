package it.unipi.MySmartRecipeBook.model;

import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipeSummary;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chefs")

public class Chef extends RegisteredUser {

    @Field("reg_date")
    @Past
    private Date registrationDate;

    @Field("new_recipes")
    private List<ChefRecipe> newRecipes;

    @Field("recipe_to_confirm")
    private List<ChefRecipe> recipesToConfirm;

    @Field("old_recipes")
    private List<ChefRecipeSummary> oldRecipes;

    @Field("tot_saves")
    private Integer totalSaves = 0;
}

