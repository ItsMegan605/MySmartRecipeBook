package it.unipi.MySmartRecipeBook.model.Mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class PendingRecipe extends BaseRecipe{
    @Field("id")
    String id;
}
