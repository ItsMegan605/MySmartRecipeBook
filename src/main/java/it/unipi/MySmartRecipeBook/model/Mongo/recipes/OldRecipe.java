package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * This model is used to store limited information about
 * previously created recipes in the chef profile page
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OldRecipe {

    @Field("id")
    String id;

    @Field("num_saves")
    private Integer numSaves;
}