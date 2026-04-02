package it.unipi.MySmartRecipeBook.dto;

import lombok.*;

/**
 * DTO for chef's ranking analytic
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChefRankAnalyticsDTO {

    private Integer rank;
    private String username;
    private Double score;
}