package it.unipi.MySmartRecipeBook.model.Mongo.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Represents a chef that is pending approval before it's accepted by the admin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingChef {

    private String id;

    private String username;

    private String name;

    private String surname;

    private String email;

    private LocalDate birthdate;


}