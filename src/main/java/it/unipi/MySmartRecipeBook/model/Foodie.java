package it.unipi.MySmartRecipeBook.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import jaca.util.ArrayList;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "foodies")

public class Foodie extends RegisteredUser {

    @Id
    private String id;

    private LocalDateTime registDate;
    /**
     * List of saved recipes (recipe IDs)
     * Handled by MongoDB as required by project specifications
     */
    private List<String> savedRecipeIds = new ArrayList<>();
}




/*
import java.util.*;

public class Foodie extends RegisteredUser {

    private Date RegistrationDate;

    public Foodie(String username, String password, String email, String name, String surname, Date registrationDate) {
        super(username, password, email, name, surname);
        RegistrationDate = registrationDate;
    }

    public Date getRegistrationDate() {
        return RegistrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        RegistrationDate = registrationDate;
    }
}
*/