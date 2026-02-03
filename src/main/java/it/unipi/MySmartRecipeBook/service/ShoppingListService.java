package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.model.ShoppingList;
import it.unipi.MySmartRecipeBook.repository.ShoppingListRepository;
import org.springframework.stereotype.Service;

@Service
public class ShoppingListService {

    //dichiao la repo perchè devo leggere i dati
    private final ShoppingListRepository shoppingListRepository;

    //costruttore per l'iniezione della dipendenza
    public ShoppingListService(ShoppingListRepository shoppingListRepository) {
        this.shoppingListRepository = shoppingListRepository;
    }

    //get di tutta la lista
    public ShoppingList getList (Integer userId) {
        return shoppingListRepository.findById(userId).orElse(new ShoppingList(userId));
    }

    // 1. Aggiungi ingrediente
    public ShoppingList addToShoppingList(Integer userId, String ingredient) {
        // Recupera la lista, oppure ne crea una nuova vuota se non esiste
        ShoppingList list = shoppingListRepository.findById(userId)
                .orElse(new ShoppingList(userId));

        list.addItem(ingredient); // Metodo helper nel Model

        return shoppingListRepository.save(list); // Salva su Redis
    }

    //rimuovi ingrediente
    public ShoppingList removeFromShoppingList(Integer userId, String ingredient) {
        ShoppingList list = shoppingListRepository.findById(userId)
                .orElse(new ShoppingList(userId));

        list.removeItem(ingredient); // Metodo helper nel Model

        return shoppingListRepository.save(list);
    }

}