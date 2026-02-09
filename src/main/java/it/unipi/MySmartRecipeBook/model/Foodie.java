package it.unipi.MySmartRecipeBook.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field; // Importante
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class Foodie extends RegisteredUser {
    //questo è quello che poi ho scoperto rompeva i coglioni
    //Chiave Primaria reale di MongoDB (_id)
    // nome diverso per non creare confusione (es. uniqueId)
    @Id
    private String uniqueId;

    // 2. Il tuo ID personalizzato ("61934")
    // Togliamo @Id da qui perché NON è la chiave primaria per Mongo
    // Usiamo @Field("id") per essere sicuri che punti al campo "id" del JSON
    @Field("id")
    private String id;

    //qui me lo sono fatto cambiare così, poi dobbiamo sistemarlo
    // //quando faremo le collezioni con id di mongo

    private Date registDate;
    private List<String> savedRecipeIds = new ArrayList<>();
}