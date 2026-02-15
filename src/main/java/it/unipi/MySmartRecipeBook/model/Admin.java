package it.unipi.MySmartRecipeBook.model;

import it.unipi.MySmartRecipeBook.model.Mongo.AdminRecipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "chefs")
public class Admin extends RegisteredUser {

    @Field("recipe_to_approve")
    private List<AdminRecipe> recipesToApprove = new ArrayList<>();

    @Field("chef_to_register")
    private List<Chef> chefToRegister = new ArrayList<>();

}
