package it.unipi.MySmartRecipeBook.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO used for updating a Chef's personal information.
 * Note: Username updates are not permitted.
 */
@Getter
@Setter
public class UpdateChefDTO {

    private String password;

    @Email (message = "Insert a valid email")
    @NotBlank(message = "Insert email")
    private String email;

    @Past
    private LocalDate birthdate;
}
