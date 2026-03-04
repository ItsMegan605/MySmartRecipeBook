package it.unipi.MySmartRecipeBook.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class TrendAnalyticsDTO {

    @Field("_id")
    private String category;

    private Integer recentCount;

    private Integer previousCount;

    private Double growthRate;

    private String trendType;
}