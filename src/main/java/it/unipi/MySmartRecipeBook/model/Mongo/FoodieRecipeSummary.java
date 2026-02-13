package it.unipi.MySmartRecipeBook.model.Mongo;

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
}
