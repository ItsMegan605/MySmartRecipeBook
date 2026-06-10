package it.unipi.MySmartRecipeBook.model.Mongo.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Lightweight embedded entity representing a newly registered chef currently wait for the admin to approve its registration request.
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