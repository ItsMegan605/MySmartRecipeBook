package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Neo4j.ChefNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ChefNeo4jRepository extends Neo4jRepository<ChefNeo4j, String> {
}
