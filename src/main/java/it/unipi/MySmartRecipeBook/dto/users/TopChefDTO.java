package it.unipi.MySmartRecipeBook.dto.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  DTO to get the top chef in a certain category
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopChefDTO{
   private String name;
   private String surname;
   private String category;
}
