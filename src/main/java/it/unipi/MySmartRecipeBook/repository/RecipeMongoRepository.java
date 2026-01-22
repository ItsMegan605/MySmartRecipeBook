package it.unipi.MySmartRecipeBook.repository;

public interface RecipeMongoRepository extends MongoRepository<RecipeMongo, String> {
    // Esempio: Spring implementa questo da solo in base al nome!
    List<RecipeMongo> findByCategory(String category);
}