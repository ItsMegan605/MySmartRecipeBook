package it.unipi.MySmartRecipeBook.security;

import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Foodie;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {


    private String password;
    private String id;
    private String name;
    private String surname;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id,
                         String name,
                         String surname,
                         String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.authorities = authorities;
    }

    //Chef
    public static UserPrincipal buildChef(Chef chef) {
        return new UserPrincipal(
                chef.getId(),
                chef.getName(),
                chef.getSurname(),
                chef.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_CHEF")
                )
        );
    }

    //Foodie
    public static UserPrincipal buildFoodie(Foodie foodie) {
        return new UserPrincipal(
                foodie.getId(),
                foodie.getName(),
                foodie.getSurname(),
                foodie.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_FOODIE")
                )
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getName() { return name; }

    public String getSurname() { return surname; }

    public String getId() { return id;
    }

    @Override
    public String getUsername() {return id; }

    @Override public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isAccountNonLocked() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override public boolean isEnabled() {
        return true;
    }
}
