package it.unipi.MySmartRecipeBook.model.Mongo;

import it.unipi.MySmartRecipeBook.model.ReducedChef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ChefRecipe {
    @Id
    private String id;

    private String title;

    @Field("presentation")
    private String presentation;

    private String category;

    @Field("prep_Time")
    private String prepTime;

    private String preparation;
    private String difficulty;

    @Field("image_url")
    private String imageURL;

    private List<Ingredient> ingredients;

    @Field("creation_date")
    private LocalDateTime creationDate;

    @Field("num_saves")
    private Integer numSaves;
}
