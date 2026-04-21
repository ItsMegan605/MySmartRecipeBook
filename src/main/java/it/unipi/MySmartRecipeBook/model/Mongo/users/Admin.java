package it.unipi.MySmartRecipeBook.model.Mongo.users;

import it.unipi.MySmartRecipeBook.model.Mongo.recipes.AdminPendingRecipe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class Admin extends RegisteredUser {

    @Indexed(unique = true)
    @NotBlank(message = "Username is required")
    @Size(max = 20)
    private String username;

    @Field("recipes_to_approve")
    private List<AdminPendingRecipe> recipesToApprove;

    @Field("chefs_to_approve")
    private List<PendingChef> chefsToApprove;

}