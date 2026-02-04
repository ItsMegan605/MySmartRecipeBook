
package it.unipi.MySmartRecipeBook.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import jakarta.validation.constraints.*;

//genero solo cio che serve, non metto direttamente lombok.data e cosi evito effetti collaterali
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class RegisteredUser {

    @NotBlank
    @Size(max = 20)
    protected String username;

    @NotBlank(message = "First name is required")
    protected String name;

    @NotBlank(message = "Last name is required")
    protected String surname;

    @NotBlank
    @Email
    @Size(max = 50)
    protected String email;

    @NotBlank
    @Size(min = 8, max = 20)
    protected String password;

    @Past(message = "Birthdate must be in the past")
    protected Date birthdate;
}


/*

public class RegisteredUser {
    private String username;
    private String password;
    private String email;
    private String name;
    private String surname;

    public RegisteredUser(String username, String password, String email, String name, String surname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
*/
