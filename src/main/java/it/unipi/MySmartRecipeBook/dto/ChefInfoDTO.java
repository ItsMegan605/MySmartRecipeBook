package it.unipi.MySmartRecipeBook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ChefInfoDTO {

    /* In the chef personal page we want to show:
        - username
        - chefName (composed by name and surname)
        - email
        - birthday

      We don't want to show the password for security reason
     */

    private String username;
    private String name;
    private String surname;
    private String email;

    @JsonProperty("birth_date")
    private LocalDate birthDate;
}
