package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Ingredient;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IngredientRepository extends MongoRepository<Ingredient, String> {
}
