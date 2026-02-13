package it.unipi.MySmartRecipeBook.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ReducedChef {

    @Field("mongo_id")
    private String mongoId;
    private String name;
    private String surname;
}
