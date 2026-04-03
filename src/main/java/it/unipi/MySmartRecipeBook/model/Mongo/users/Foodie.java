package it.unipi.MySmartRecipeBook.model.Mongo.users;

import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field; // Important
import java.util.Date;
import java.util.List;

/**
 * Represents a foodie user in the system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@CompoundIndexes({
        // Index to optimize queries filtering by chef inside saved recipes
        @CompoundIndex(name = "saved_idx", def = "{'saved_recipes.chef.id': 1}")
})

@Document(collection = "foodies")
public class Foodie extends RegisteredUser {

    /**
     * Registration date of the foodie.
     */
    @Field("registration_date")
    private Date registrationDate;

    /**
     * List of recipes saved by the foodie.
     */
    @Field("saved_recipes")
    private List<FoodieRecipeSummary> savedRecipes;

}