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
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id, String username, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    //Chef
    public static UserPrincipal buildChef(Chef chef) {
        return new UserPrincipal(
                chef.getId(),
                chef.getUsername(),
                chef.getPassword(),
                Collections.singletonList (new SimpleGrantedAuthority("ROLE_CHEF"))
        );
    }

    //Foodie
    public static UserPrincipal buildFoodie(Foodie foodie) {
        return new UserPrincipal(
                foodie.getId(),
                foodie.getUsername(),
                foodie.getPassword(),
                Collections.singletonList (new SimpleGrantedAuthority("ROLE_FOODIE"))
        );
    }

    //ADMIN (senza campo role nel DB)
    public static UserPrincipal buildAdmin(Chef admin) {
        return new UserPrincipal(
                admin.getId(),
                admin.getUsername(),
                admin.getPassword(),
                Collections.singletonList (new SimpleGrantedAuthority("ROLE_ADMIN"))
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

    public String getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }
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
