package it.unipi.MySmartRecipeBook.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("ingredients")
public class Ingredient {

    @Id
    private String id;

    @Field("nome") // Questo deve coincidere con la chiave JSON del documento
    private String name;
}

