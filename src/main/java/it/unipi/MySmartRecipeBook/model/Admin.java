package it.unipi.MySmartRecipeBook.model;

import it.unipi.MySmartRecipeBook.model.Mongo.BaseRecipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Document(collection = "chefs")
public class Admin extends RegisteredUser {

    @Field("recipes_to_approve")
    private List<BaseRecipe> recipesToApprove;

    @Field("chefs_to_approve")
    private List<Chef> chefsToApprove;

}
