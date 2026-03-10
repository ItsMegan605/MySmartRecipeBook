package it.unipi.MySmartRecipeBook.model.Mongo.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingChef {

    private String id;

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
    private LocalDate birthdate;

    @Field("reg_date")
    private LocalDate registrationDate;


}
