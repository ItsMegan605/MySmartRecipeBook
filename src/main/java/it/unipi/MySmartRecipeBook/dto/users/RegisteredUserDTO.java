package it.unipi.MySmartRecipeBook.dto.users;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO for users registration with all mandatory fields
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisteredUserDTO {

    @NotBlank(message = "Insert username")
    private String username;

    @NotBlank(message = "Insert name")
    private String name;

    @NotBlank(message = "Insert surname")
    private String surname;

    @Email(message = "Insert a valid email")
    @NotBlank(message = "Insert email")
    private String email;

    @NotBlank(message = "Insert password")
    private String password;

    @Past(message = "Birthdate must be in the past")
    @NotNull(message = "Insert a valid birthdate")
    private LocalDate birthdate;

}
