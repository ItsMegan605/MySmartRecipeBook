package it.unipi.MySmartRecipeBook.model.Mongo.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lightweight embedded entity representing a newly registered chef currently
 * waiting for the admin to approve its registration request.
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

}