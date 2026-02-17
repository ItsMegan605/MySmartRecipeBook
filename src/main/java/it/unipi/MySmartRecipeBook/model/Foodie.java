package it.unipi.MySmartRecipeBook.model;

import it.unipi.MySmartRecipeBook.model.Mongo.FoodieRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.FoodieRecipeSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field; // Importante
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class Foodie extends RegisteredUser {

    @Field("registration_date")
    private Date registrationDate;

    @Field("new_saved")
    private List<FoodieRecipe> newSavedRecipes;

    @Field("old_saved")
    private List<FoodieRecipeSummary> oldSavedRecipes;
}