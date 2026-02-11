package it.unipi.MySmartRecipeBook.service;

import it.unipi.MySmartRecipeBook.dto.FoodieResponseDTO;
import it.unipi.MySmartRecipeBook.dto.UpdateFoodieDTO;
import it.unipi.MySmartRecipeBook.model.Foodie;
import it.unipi.MySmartRecipeBook.repository.FoodieRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class FoodieService {

    private final FoodieRepository foodieRepository;
    private final PasswordEncoder passwordEncoder;

    public FoodieService(FoodieRepository foodieRepository,
                         PasswordEncoder passwordEncoder) {
        this.foodieRepository = foodieRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* =========================
       PROFILE MANAGEMENT
       ========================= */

    public FoodieResponseDTO getByUsername(String username) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        return mapToResponse(foodie);
    }

    public FoodieResponseDTO updateFoodie(String username,
                                          UpdateFoodieDTO dto) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if (dto.getName() != null)
            foodie.setName(dto.getName());

        if (dto.getSurname() != null)
            foodie.setSurname(dto.getSurname());

        if (dto.getEmail() != null)
            foodie.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            foodie.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getBirthdate() != null)
            foodie.setBirthdate(dto.getBirthdate());

        foodieRepository.save(foodie);

        return mapToResponse(foodie);
    }

    public void deleteFoodie(String username) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        foodieRepository.delete(foodie);
    }

    /* =========================
       SAVED RECIPES
       ========================= */

    public void saveRecipe(String username, String recipeId) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        if (!foodie.getSavedRecipeIds().contains(recipeId)) {
            foodie.getSavedRecipeIds().add(recipeId);
            foodieRepository.save(foodie);
        }
    }

    public void removeSavedRecipe(String username, String recipeId) {

        Foodie foodie = foodieRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Foodie not found"));

        foodie.getSavedRecipeIds().remove(recipeId);
        foodieRepository.save(foodie);
    }

    /* =========================
       MAPPING
       ========================= */

    private FoodieResponseDTO mapToResponse(Foodie foodie) {

        return new FoodieResponseDTO(
                foodie.getUsername(),
                foodie.getName(),
                foodie.getSurname(),
                foodie.getEmail()
        );
    }
}
