package it.unipi.MySmartRecipeBook.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * DTO used for updating a Chef's personal information.
 * Note: Username updates are not permitted.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateChefDTO {

    private String password;

    @Email (message = "Insert a valid email")
    private String email;

    @Past
    private LocalDate birthdate;

    /**
     * Checks if the updatable fields in the DTO are empty or null.
     * @return true if email, password, birthdate are empty or null, false otherwise.
     */
    public boolean isEmpty() {
        return !StringUtils.hasText(this.email) &&
                !StringUtils.hasText(this.password) &&
                this.birthdate == null;
    }
}
