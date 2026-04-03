package it.unipi.MySmartRecipeBook.model.Mongo.users;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * Base class representing a registered user in the system.
 *
 * It contains common attributes shared by all user types
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class RegisteredUser {

    /**
     * Unique identifier of the user (MongoDB document ID).
     */
    @Id
    private String id;

    /**
     * Username of the user.
     * Must be unique across the system.
     */
    @Indexed(unique = true)
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
    private LocalDate birthDate;
}