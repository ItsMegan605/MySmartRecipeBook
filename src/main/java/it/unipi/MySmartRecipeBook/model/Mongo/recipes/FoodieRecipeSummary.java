package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import it.unipi.MySmartRecipeBook.model.Mongo.users.ReducedChef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

/**
 * Represents a lightweight, embedded sub-document stored within the Foodie entity.
 * This class implements the "partial embedding" pattern.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodieRecipeSummary {

    @Field("id")
    private String id;

    private String title;

    private String category;

    private String difficulty;

    @Field("image_url")
    private String imageURL;

    @Field("saving_date")
    private LocalDate savingDate;

    @Field("chef")
    private ReducedChef chef;
}

