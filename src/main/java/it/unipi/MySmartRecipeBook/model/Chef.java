package it.unipi.MySmartRecipeBook.model;

import java.util.Date;

public class Chef extends RegisteredUser {
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
