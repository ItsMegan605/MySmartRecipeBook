package it.unipi.MySmartRecipeBook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * Analytics for total of registered foodies for each year
 */
@Data
public class YearAnalyticsDTO {

    @Field("_id")
    private Integer year;

    @JsonProperty("total Registered Foodies")
    private int totalRegisteredFoodies;

    private List<MonthAnalyticsDTO> monthAnalyticsDTOList;
}
