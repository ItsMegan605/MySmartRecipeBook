package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Represents a summary view of a chef's recipe.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChefRecipeSummary {


    @Field("id")
    private String id;

    private String title;

    @Field("image_url")
    private String imageURL;

    @Field("creation_date")
    private LocalDateTime creationDate;

    @Field("num_saves")
    private Integer numSaves;
}


