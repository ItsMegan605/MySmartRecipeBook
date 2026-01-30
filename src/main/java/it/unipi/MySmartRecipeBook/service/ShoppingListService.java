package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.model.ShoppingList;
import it.unipi.MySmartRecipeBook.repository.ShoppingListRepository;
import org.springframework.stereotype.Service;

@Service
public class ShoppingListService {

    //dichiao la repo perchè devo leggere i dati
    private final ShoppingListRepository repository;

    //costruttore per l'iniezione della dipendenza
    public ShoppingListService(ShoppingListRepository repository) {
        this.repository = repository;
    }

    // 1. Aggiungi ingrediente
    public ShoppingList addToShoppingList(Integer userId, String ingredient) {
        // Recupera la lista, oppure ne crea una nuova vuota se non esiste
        ShoppingList list = repository.findById(userId)
                .orElse(new ShoppingList(userId));

        list.addItem(ingredient); // Metodo helper nel Model

        return repository.save(list); // Salva su Redis
    }

    //rimuovi ingrediente
    public ShoppingList removeFromShoppingList(Integer userId, String ingredient) {
        ShoppingList list = repository.findById(userId)
                .orElse(new ShoppingList(userId));

        list.removeItem(ingredient); // Metodo helper nel Model

        return repository.save(list);
    }

    //leggi la lista (per mostrarla all'utente)
    public ShoppingList getCart(Integer userId) {
        return repository.findById(userId)
                .orElse(new ShoppingList(userId)); // Ritorna lista vuota se non c'è nulla
    }
}