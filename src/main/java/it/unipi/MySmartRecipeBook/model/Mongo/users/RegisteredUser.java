
package it.unipi.MySmartRecipeBook.model.Mongo.users;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class RegisteredUser {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Username is required")
    @Size(max = 20)
    private String username;

    @NotBlank(message = "First name is required")
    private String name;

    @NotBlank(message = "Last name is required")
    private String surname;

    @NotBlank (message = "E-mail is required")
    @Email
    @Size(max = 50)
    private String email;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @Past(message = "Birthdate must be in the past")
    private LocalDate birthDate;
}

