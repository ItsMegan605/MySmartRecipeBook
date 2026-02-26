package it.unipi.MySmartRecipeBook.model.Mongo;

import it.unipi.MySmartRecipeBook.model.ReducedChef;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class FoodieRecipeSummary {

    @Id
    private String id;

    private String title;
    private String category;
    private String difficulty;

    @Field("image_url")
    private String imageURL;

    @Field("saving_date")
    private LocalDate savingDate;

    // Ci serve nel momento in cui dobbiamo andare a decrementare il contatore delle ricette di quello chef salvate
    @Field("chef")
    private ReducedChef chef;
}
