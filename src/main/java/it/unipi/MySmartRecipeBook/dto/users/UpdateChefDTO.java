package it.unipi.MySmartRecipeBook.dto.users;

import jakarta.validation.constraints.Email;
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

    private String password;

    @Email
    private String email;
    private LocalDate birthdate;
}
