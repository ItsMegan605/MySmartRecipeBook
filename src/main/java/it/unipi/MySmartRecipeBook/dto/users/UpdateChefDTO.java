package it.unipi.MySmartRecipeBook.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * We want to allow chef to change their personal information.
 * We don't allow chefs to change their username
 */
@Getter
@Setter
public class UpdateChefDTO {

    private String password;

    @Email
    private String email;

    @Past
    private LocalDate birthdate;
}
