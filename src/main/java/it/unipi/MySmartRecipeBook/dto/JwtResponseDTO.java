package it.unipi.MySmartRecipeBook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO representing the JWT response payload
 * on successful authentication.
 * */
@Getter
@AllArgsConstructor

//Token structure and user fields
public class JwtResponseDTO {
    private String token;
    private String id;
    private String username;
    private Object roles;

}
