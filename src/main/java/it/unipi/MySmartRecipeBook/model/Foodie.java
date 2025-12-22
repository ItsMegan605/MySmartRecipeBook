package it.unipi.MySmartRecipeBook.model;
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
