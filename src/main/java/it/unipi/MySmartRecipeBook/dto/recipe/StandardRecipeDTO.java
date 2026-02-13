package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class StandardRecipeDTO extends BaseRecipeDTO{

    @JsonProperty("creation_date")
    private LocalDate creationDate;
}