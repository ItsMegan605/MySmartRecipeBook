package it.unipi.MySmartRecipeBook.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  DTO to get the top chef in a certain category
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TopChefDTO{
    String name;
    String surname;
    String category;
}
