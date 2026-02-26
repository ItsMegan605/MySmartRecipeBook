package it.unipi.MySmartRecipeBook.model;

import it.unipi.MySmartRecipeBook.model.Mongo.FoodieRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.FoodieRecipeSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field; // Importante
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@CompoundIndexes({
        @CompoundIndex(name = "new_saved_idx", def = "{'new_saved.chef.id': 1}"),
        @CompoundIndex(name = "old_saved_idx", def = "{'old_saved.id': 1}")
})

@Document(collection = "users")
public class Foodie extends RegisteredUser {

    @Field("registration_date")
    private Date registrationDate;

    @Field("new_saved")
    private List<FoodieRecipe> newSavedRecipes;

    @Field("old_saved")
    private List<FoodieRecipeSummary> oldSavedRecipes;

    @Version
    private long version;
}