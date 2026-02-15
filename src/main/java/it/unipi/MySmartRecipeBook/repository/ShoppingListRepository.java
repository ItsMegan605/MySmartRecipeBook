package it.unipi.MySmartRecipeBook.repository;
//la shopping list è su redis e basta

import it.unipi.MySmartRecipeBook.model.Redis.ShoppingList;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingListRepository extends CrudRepository<ShoppingList, Integer> {
}
