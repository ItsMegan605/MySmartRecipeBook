package it.unipi.MySmartRecipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO for the security functions
 */
@Getter
@AllArgsConstructor

//struttura e campi del token
public class JwtResponseDTO {
    private String token;
    private String id;
    private String username;
    private Object roles;

}
