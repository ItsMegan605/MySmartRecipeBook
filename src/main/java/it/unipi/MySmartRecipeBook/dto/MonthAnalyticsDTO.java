package it.unipi.MySmartRecipeBook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * DTO to get the monthly subscribed foodies
 */

@Data
public class MonthAnalyticsDTO {

    @Field("_id")
    private String month;

    @JsonProperty("Total of new monthly Foodies")
    private int totalFoodies;
}
