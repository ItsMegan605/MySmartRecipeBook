package it.unipi.MySmartRecipeBook.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO used for updating a Foodie's personal information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFoodieDTO{


    private String name;

    private String surname;

    @Email
    private String email;

    @Past
    private LocalDate birthdate;

    private String password;

}
