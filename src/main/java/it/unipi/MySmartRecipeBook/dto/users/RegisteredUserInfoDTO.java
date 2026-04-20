package it.unipi.MySmartRecipeBook.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO used to display a registered user's profile information.
 * */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredUserInfoDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @Email (message = "Insert a valid email")
    @NotBlank
    private String email;

    @Past
    private LocalDate birthdate;
}
