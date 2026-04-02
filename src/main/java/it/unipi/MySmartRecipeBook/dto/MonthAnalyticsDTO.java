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

    @Field ("_id") //for mongo query
    private String month;

    @JsonProperty("tot_new_monthly_foodies")
    private int totalFoodies;
}
