package it.unipi.MySmartRecipeBook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * DTO to get the monthly subscribed foodies
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthAnalyticsDTO {
    
    private String month;

    @JsonProperty("Total of new monthly Foodies")
    private int totalFoodies;
}
