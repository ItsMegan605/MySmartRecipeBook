package it.unipi.MySmartRecipeBook.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * DTO containing basic Chef information.
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChefInfoDTO {
    private String id;
    private String name;
    private String surname;
}
