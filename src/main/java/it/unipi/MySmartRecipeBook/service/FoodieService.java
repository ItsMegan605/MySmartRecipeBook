package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.CreateFoodieDTO;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FoodieService {

    private final FoodieRepository foodieRepository;

    public FoodieService(FoodieRepository foodieRepository) {
        this.foodieRepository = foodieRepository;
    }

    /* =========================
       PROFILE MANAGEMENT
       ========================= */

    public Foodie createFoodie(CreateFoodieDTO dto) {

        Foodie foodie = new Foodie();
        foodie.setName(dto.getName());
        foodie.setSurname(dto.getSurname());
        foodie.setUsername(dto.getUsername());
        foodie.setEmail(dto.getEmail());
        foodie.setPassword(dto.getPassword());
        foodie.setBirthdate(dto.getBirthdate());
        foodie.setRegistDate(LocalDateTime.now());

        return foodieRepository.save(foodie);
    }

    public Foodie updateFoodie(String username, CreateFoodieDTO dto) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        foodie.setName(dto.getName());
        foodie.setSurname(dto.getSurname());
        foodie.setEmail(dto.getEmail());
        foodie.setPassword(dto.getPassword());
        foodie.setBirthdate(dto.getBirthdate());

        return foodieRepository.save(foodie);
    }

    public void deleteFoodie(String username) {
        foodieRepository.findByUsername(username)
                .ifPresent(foodieRepository::delete);
    }

    public Optional<Foodie> getFoodieByUsername(String username) {
        return foodieRepository.findByUsername(username);
    }

    /* =========================
       SAVED RECIPES (MongoDB)
       ========================= */

    public void saveRecipe(String foodieUsername, String recipeId) {

        Foodie foodie = foodieRepository.findByUsername(foodieUsername)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if (!foodie.getSavedRecipeIds().contains(recipeId)) {
            foodie.getSavedRecipeIds().add(recipeId);
            foodieRepository.save(foodie);
        }
    }

    public void removeSavedRecipe(String foodieUsername, String recipeId) {

        Foodie foodie = foodieRepository.findByUsername(foodieUsername)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        foodie.getSavedRecipeIds().remove(recipeId);
        foodieRepository.save(foodie);
    }
}


/*
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
*/