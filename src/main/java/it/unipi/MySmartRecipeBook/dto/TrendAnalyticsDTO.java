package it.unipi.MySmartRecipeBook.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * DTO with parameters for trend analytics
 */
@Data
public class TrendAnalyticsDTO {

    @Field("_id")
    private String category;

    private Integer recentCount;

    private Integer previousCount;

    private Double growthRate;

    private String trendType;
}