package it.unipi.MySmartRecipeBook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * Analytics for total of registered foodies for each year
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class YearAnalyticsDTO {
    @Field ("_id") //for mongo query
    private Integer year;

    @JsonProperty("total_registered_foodies")
    private int totalRegisteredFoodies;

    private List<MonthAnalyticsDTO> monthAnalyticsDTOList;
}
