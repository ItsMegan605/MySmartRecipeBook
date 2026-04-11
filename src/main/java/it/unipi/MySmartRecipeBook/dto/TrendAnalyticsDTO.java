package it.unipi.MySmartRecipeBook.dto;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * DTO representing the trending statistics
 * and growth rates for recipe categories.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrendAnalyticsDTO {

    @Field("_id")
    private String category;

    private Integer recentCount;

    private Integer previousCount;

    private Double growthRate;

    private String trendType;
}