package it.unipi.MySmartRecipeBook.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document("ingredients")
public class Ingredient {

    // Per ora ho messo che l'id coincide con il nome perchè deve essere univoco, però possiamo decidere diversamente
    @Id
    String name;
}

