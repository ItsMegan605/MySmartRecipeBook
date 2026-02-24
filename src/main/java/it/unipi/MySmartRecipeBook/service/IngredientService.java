package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.model.Ingredient;
import it.unipi.MySmartRecipeBook.repository.IngredientRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class IngredientService {

    private Set<String> allowedIngredients = new HashSet<String>();

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    // Dice a Spring di fare questa come prima cosa prima di accettare richieste degli utenti - possiamo dire che è un
    // costrutto che ci ha detto l'AI così Ducange è contento che ammettiamo le nostre colpe
    @PostConstruct
    private void inizializeAllowedIngredients(){

        List<Ingredient> ingredients = ingredientRepository.findAll();
        for(Ingredient ingredient : ingredients){
            allowedIngredients.add(ingredient.getName());
        }
    }

    public boolean isValidIngredient(String ingredientName){

        String ingredient = ingredientName.toLowerCase();
        return allowedIngredients.contains(ingredient);
    }


}
