package it.unipi.MySmartRecipeBook.model.Mongo.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents a reduced version of a Chef.
 *
 * This model is used to embed only essential chef information
 * inside other documents to avoid full data duplication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReducedChef {

    /**
     * Unique identifier of the chef.
     */
    @Field("id")
    private String id;

    /**
     * First name of the chef.
     */
    private String name;

    /**
     * Last name of the chef.
     */
    private String surname;
}