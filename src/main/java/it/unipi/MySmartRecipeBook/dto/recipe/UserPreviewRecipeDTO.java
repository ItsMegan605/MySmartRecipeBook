package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;

//recipe's preview for foodies

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class UserPreviewRecipeDTO extends PreviewRecipeDTO {

    @JsonProperty("chef_name")
    private String chefName;
}
