package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import org.springframework.stereotype.Service;

@Service
public class FoodieService {
    private final FoodieRepository foodieRepository;

    public FoodieService(FoodieRepository foodieRepository) {
        this.foodieRepository = foodieRepository; //tecnicamente li abbiamo su mongo ma non necessaro psecificarlo ?
    }

    public Foodie login (String username, String password) {
        return foodieRepository.findByUsernameAndPassword(username, password)
                .orElse(null);
    }

    //forse ci va il metodo save delle ricette? non lo so
}
