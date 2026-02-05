package it.unipi.MySmartRecipeBook.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chefs")

public class Chef extends RegisteredUser {

    @Id
    private String id;

    private Date registeredDate;
}

/*
import java.util.Date;

public class  Chef extends RegisteredUser {
    private Date RegistrationDate;

    public Chef(String username, String password, String email, String name, String surname, Date registrationDate) {
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