package it.unipi.MySmartRecipeBook.dto;

import lombok.Data;

@Data
public class ChefRankAnalyticsDTO {

    private Integer rank;
    private String username;
    private Double score;
}