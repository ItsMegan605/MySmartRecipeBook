package it.unipi.MySmartRecipeBook.model.Mongo.users;

import it.unipi.MySmartRecipeBook.model.Mongo.recipes.PendingRecipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * Represents an admin user in the system.
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Document(collection = "chefs")
public class Admin extends RegisteredUser {

    @Field("recipes_to_approve")
    private List<PendingRecipe> recipesToApprove;

    @Field("chefs_to_approve")
    private List<PendingChef> chefsToApprove;

}