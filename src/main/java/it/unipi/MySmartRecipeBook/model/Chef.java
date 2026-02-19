package it.unipi.MySmartRecipeBook.model;

import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipeSummary;
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
    private List<ChefRecipe> newRecipes;

    @Field("recipe_to_confirm")
    private List<ChefRecipe> recipesToConfirm;

    @Field("tot_saves")
    private Integer totalSaves = 0;

    @Field("tot_recipes")
    private Integer totalRecipes = 0;
}

