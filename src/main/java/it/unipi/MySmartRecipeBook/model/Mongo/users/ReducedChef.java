package it.unipi.MySmartRecipeBook.model.Mongo.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents a reduced version of a Chef.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReducedChef {


    @Field("id")
    private String id;

    private String name;

    private String surname;
}