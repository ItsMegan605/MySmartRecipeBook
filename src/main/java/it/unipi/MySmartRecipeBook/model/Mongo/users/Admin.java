package it.unipi.MySmartRecipeBook.model.Mongo.users;

import it.unipi.MySmartRecipeBook.model.Mongo.recipes.AdminPendingRecipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * Represents the admin user in the system.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Document(collection = "chefs")
public class Admin {

    @Id
    private String id;

    private String password;

    @Indexed(unique = true)
    private String username;

    @Field("recipes_to_approve")
    private List<AdminPendingRecipe> recipesToApprove;

    @Field("chefs_to_approve")
    private List<PendingChef> chefsToApprove;

}