package it.unipi.MySmartRecipeBook.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Chef indormation DTO
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter


public class ChefInfoDTO {
    String id;
    String name;
    String surname;
}
