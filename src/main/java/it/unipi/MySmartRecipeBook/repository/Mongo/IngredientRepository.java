package it.unipi.MySmartRecipeBook.repository.Mongo;

import it.unipi.MySmartRecipeBook.model.Mongo.ingredients.Ingredient;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IngredientRepository extends MongoRepository<Ingredient, String> {
}
