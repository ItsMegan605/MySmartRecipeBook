package it.unipi.MySmartRecipeBook.dto;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class AnalyticsDTO {

    @Field("_id")
    private String id;

    private long number;
}
