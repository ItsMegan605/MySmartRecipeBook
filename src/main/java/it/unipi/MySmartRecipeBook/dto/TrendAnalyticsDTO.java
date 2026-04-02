package it.unipi.MySmartRecipeBook.dto;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * DTO with parameters for trend analytics
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrendAnalyticsDTO {

    private String category;

    private Integer recentCount;

    private Integer previousCount;

    private Double growthRate;

    private String trendType;
}