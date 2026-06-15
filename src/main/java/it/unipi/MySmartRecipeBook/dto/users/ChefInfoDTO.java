package it.unipi.MySmartRecipeBook.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * DTO containing basic Chef information.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChefInfoDTO {

    private String id;

    private String name;

    private String surname;
}
