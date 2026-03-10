package it.unipi.MySmartRecipeBook.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateChefDTO {

    /* We want to allow chef to change their personal informations. In particular, the user
    can choose to modify one or more of the following parameters:

        - name
        - surname
        - password
        - email
        - birthday

     We don't allow chefs to change their username
     */

    @NotBlank
    private String password;

    @Email
    @NotBlank
    private String email;

    @NotNull
    @Past
    private LocalDate birthdate;
}
