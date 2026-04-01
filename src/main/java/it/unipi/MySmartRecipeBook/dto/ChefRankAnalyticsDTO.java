package it.unipi.MySmartRecipeBook.dto;

import lombok.Data;

/**
 * DTO for chef's ranking analytic
 */

@Data
public class ChefRankAnalyticsDTO {

    private Integer rank;
    private String username;
    private Double score;
}