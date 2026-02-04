package it.unipi.MySmartRecipeBook.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateChefDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @Email
    private String email;
}
