package it.unipi.MySmartRecipeBook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateRecipeDTO {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String category;

    @NotNull
    private Integer prepTime;

    @NotBlank
    private String difficulty;

    private String imageURL;

    @NotBlank
    private String preparation;

    private List<String> ingredients;

    @NotBlank
    private String chefUsername;
}
