package it.unipi.MySmartRecipeBook.model.Mongo;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoodieRecipe extends RecipeMongo {

    @Field("saving_date")
    private LocalDate savingDate;
}
