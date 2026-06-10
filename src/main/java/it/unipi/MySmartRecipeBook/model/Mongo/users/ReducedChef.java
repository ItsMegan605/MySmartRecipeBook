package it.unipi.MySmartRecipeBook.model.Mongo.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A partially embedded projection of the Chef entity, storing only essential identity fields to optimize read performance and avoid secondary database lookups.
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