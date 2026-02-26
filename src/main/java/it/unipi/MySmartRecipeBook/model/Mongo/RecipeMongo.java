package it.unipi.MySmartRecipeBook.model.Mongo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "recipes")

@CompoundIndexes({
        // Indice per filtrare per Chef E ordinare per Data (veloce per la "Vetrina")
        @CompoundIndex(name = "chefDate_idx", def = "{'chef.id': 1, 'creation_date': -1}"),

        // Indice per filtrare per Chef E ordinare per Like (veloce per "Most Liked")
        @CompoundIndex(name = "chefPopularity_idx", def = "{'chef.id': 1, 'num_saves': -1}")
})

public class RecipeMongo extends BaseRecipe{

    @Field("num_saves")
    private Integer numSaves;
}
