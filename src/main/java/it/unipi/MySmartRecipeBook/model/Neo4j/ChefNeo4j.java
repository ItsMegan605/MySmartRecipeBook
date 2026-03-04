package it.unipi.MySmartRecipeBook.model.Neo4j;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Node("Chef")
public class ChefNeo4j {

    @Id
    private String id;
    private String mongoId;
    private String name;
    private String surname;
}
