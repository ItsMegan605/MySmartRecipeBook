package it.unipi.MySmartRecipeBook.dto.foodie;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStandardFoodieDTO extends StandardFoodieDTO {

    @NotBlank
    private String password;

}
