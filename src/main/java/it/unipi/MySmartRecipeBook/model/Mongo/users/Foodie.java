package it.unipi.MySmartRecipeBook.model.Mongo.users;

import it.unipi.MySmartRecipeBook.model.Mongo.recipes.FoodieRecipeSummary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Date;
import java.util.List;

/**
 * Represents a foodie user in the system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "foodies")
public class Foodie extends RegisteredUser {

    @NotBlank(message = "Username is required")
    @Size(max = 20)
    @Indexed(unique = true)
    private String username;

    @Field("registration_date")
    private Date registrationDate;

    @Field("saved_recipes")
    private List<FoodieRecipeSummary> savedRecipes;

}


