package it.unipi.MySmartRecipeBook.dto.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Pending recipe preview for admin approval.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingRecipeDTO {

    private String title;

    private String chef;

    @JsonProperty("creation_date")
    private LocalDate creationDate;
}