package it.unipi.MySmartRecipeBook.security;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents the authenticated user inside Spring Security.
 * This class implements UserDetails and adapts application-specific
 * user data (Chef, Foodie, Admin) into a format usable by Spring Security.
 * Each time a user logs in, Spring Security creates a UserPrincipal
 * instance containing user information and roles.
 */
public class UserPrincipal implements UserDetails {

    private final String password;

    private final String id;

    private final String username;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id, String username, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Factory method to build a UserPrincipal from a Chef.
     * Automatically assigns ROLE_CHEF.
     *
     * @param chef the Chef entity
     * @return a UserPrincipal instance
     */
    public static UserPrincipal buildChef(Chef chef) {
        return new UserPrincipal(
                chef.getId(),
                chef.getUsername(),
                chef.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CHEF"))
        );
    }

    /**
     * Factory method to build a UserPrincipal from a Foodie.
     * Automatically assigns ROLE_FOODIE.
     *
     * @param foodie the Foodie entity
     * @return a UserPrincipal instance
     */
    public static UserPrincipal buildFoodie(Foodie foodie) {
        return new UserPrincipal(
                foodie.getId(),
                foodie.getUsername(),
                foodie.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_FOODIE"))
        );
    }

    /**
     * Factory method to build a UserPrincipal for Admin.
     * Automatically assigns ROLE_ADMIN.
     *
     * @param admin the Admin entity (stored as Chef)
     * @return a UserPrincipal instance
     */
    public static UserPrincipal buildAdmin(Chef admin) {
        return new UserPrincipal(
                admin.getId(),
                admin.getUsername(),
                admin.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    /**
     * Returns the authorities (roles) of the user.
     * Used by Spring Security to check access permissions.
     *
     * @return collection of authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the user password.
     * Used during authentication to verify credentials.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user ID.
     * Not part of UserDetails, but useful for application logic.
     *
     * @return the user ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the username used for login.
     *
     * @return the username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the account is non-expired.
     *
     * @return always true in this implementation
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is non-locked.
     *
     * @return always true in this implementation
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the credentials are non-expired.
     *
     * @return always true in this implementation
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the account is enabled.
     *
     * @return always true in this implementation
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
