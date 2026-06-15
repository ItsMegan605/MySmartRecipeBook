package it.unipi.MySmartRecipeBook.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO representing a chef registration request pending admin approval.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingChefDTO {

    private String username;

    private String name;

    private String surname;
}