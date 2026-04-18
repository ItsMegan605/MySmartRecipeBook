package it.unipi.MySmartRecipeBook.utils.conversionFunctions;

import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserDTO;
import it.unipi.MySmartRecipeBook.dto.users.RegisteredUserInfoDTO;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * * Utility class for Foodie-related entity and DTO conversions.
 */
@Component
public class FoodieUtilityFunctions {

    private final PasswordEncoder passwordEncoder;

    public FoodieUtilityFunctions(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Converts a registration DTO into a Foodie entity, encrypting the password.
     * @param foodieDTO the registration data
     * @return the Foodie entity
     */
    public Foodie createFoodieEntity (RegisteredUserDTO foodieDTO){

        Foodie foodie = new Foodie();
        foodie.setUsername(foodieDTO.getUsername());
        foodie.setEmail(foodieDTO.getEmail());
        foodie.setPassword(passwordEncoder.encode(foodieDTO.getPassword()));

        foodie.setName(foodieDTO.getName());
        foodie.setSurname(foodieDTO.getSurname());
        foodie.setBirthdate(foodieDTO.getBirthdate());
        foodie.setRegistrationDate(new Date());

        return foodie;
    }

    /**
     * Converts a Foodie entity to a DTO for profile display.
     * @param foodie the Foodie entity
     * @return the registered user info DTO
     */
    public RegisteredUserInfoDTO entityToFoodieDTO (Foodie foodie) {

        return new RegisteredUserInfoDTO(
                foodie.getUsername(),
                foodie.getName(),
                foodie.getSurname(),
                foodie.getEmail(),
                foodie.getBirthdate()
        );
    }

}
