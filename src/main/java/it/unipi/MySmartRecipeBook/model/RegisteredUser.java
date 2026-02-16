
package it.unipi.MySmartRecipeBook.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class RegisteredUser {

    @Id
    private String id;

    @NotBlank(message = "Username is required")
    @Size(max = 20)
    protected String username;

    @NotBlank(message = "First name is required")
    protected String name;

    @NotBlank(message = "Last name is required")
    protected String surname;

    @NotBlank (message = "E-mail is required")
    @Email
    @Size(max = 50)
    protected String email;

    @NotBlank
    @Size(min = 8, max = 20)
    protected String password;

    @Past(message = "Birthdate must be in the past")
    protected LocalDate birthdate;
}

