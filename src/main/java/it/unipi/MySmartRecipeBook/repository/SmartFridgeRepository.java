package it.unipi.MySmartRecipeBook.repository;

import it.unipi.MySmartRecipeBook.model.Redis.SmartFridge;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmartFridgeRepository extends CrudRepository<SmartFridge, Integer> {
    // Redis repository base: fornisce save(), findById(), deleteById()...
}

