package it.unipi.MySmartRecipeBook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * DTO for foodies and chef's login request
 */
@Getter
public class LoginRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
