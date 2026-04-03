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

/**
 * Represents a chef that is pending approval.
 *
 * This model is used to temporarily store user data
 * before the chef account is officially approved by the admin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingChef {

    /**
     * Unique identifier of the pending chef.
     */
    private String id;

    /**
     * Username chosen by the user.
     */
    @NotBlank(message = "Username is required")
    @Size(max = 20)
    private String username;

    /**
     * First name of the user.
     */
    @NotBlank(message = "First name is required")
    private String name;

    /**
     * Last name of the user.
     */
    @NotBlank(message = "Last name is required")
    private String surname;

    /**
     * Email address of the user.
     */
    @NotBlank(message = "E-mail is required")
    @Email
    @Size(max = 50)
    private String email;

    /**
     * User password.
     */
    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    /**
     * Birthdate of the user.
     * Must be a past date.
     */
    @Past(message = "Birthdate must be in the past")
    private LocalDate birthdate;

    /**
     * Registration date of the user.
     */
    @Field("reg_date")
    private LocalDate registrationDate;

}