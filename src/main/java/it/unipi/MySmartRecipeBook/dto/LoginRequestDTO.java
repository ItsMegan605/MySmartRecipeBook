package it.unipi.MySmartRecipeBook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for handling login requests for both Foodies and Chefs.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Must be a valid username")
    private String username;

    @NotBlank(message = "Must be a valid password")
    private String password;
}
