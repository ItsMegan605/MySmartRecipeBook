package it.unipi.MySmartRecipeBook.model;

import it.unipi.MySmartRecipeBook.model.Mongo.ChefRecipe;
import it.unipi.MySmartRecipeBook.model.Mongo.RecipeMongo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Admin extends RegisteredUser {

    @Field("recipe_to_approve")
    private List<RecipeMongo> recipesToApprove = new ArrayList<>();

    @Field("chef_to_register")
    private List<Chef> chefToRegister = new ArrayList<>();

}
