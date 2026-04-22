package it.unipi.MySmartRecipeBook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * DTO for handling login requests for both Foodies and Chefs.
 */

@Getter
public class LoginRequestDTO {

    @NotBlank(message = "Must be a valid username")
    private String username;

    @NotBlank(message = "Must be a valid password")
    private String password;
}
