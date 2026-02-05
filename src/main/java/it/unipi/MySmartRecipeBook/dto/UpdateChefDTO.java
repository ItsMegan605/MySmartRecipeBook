package it.unipi.MySmartRecipeBook.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UpdateChefDTO {

    private String name;
    private String surname;

    @Email
    private String email;

    private String password;
    private Date birthdate;
}
