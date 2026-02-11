package it.unipi.MySmartRecipeBook.security;

import it.unipi.MySmartRecipeBook.model.Chef;
import it.unipi.MySmartRecipeBook.model.Foodie;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String username,
                         String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    // 👨‍🍳 Costruzione Chef
    public static UserPrincipal buildChef(Chef chef) {
        return new UserPrincipal(
                chef.getUsername(),
                chef.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_CHEF")
                )
        );
    }

    // 👤 Costruzione Foodie
    public static UserPrincipal buildFoodie(Foodie foodie) {
        return new UserPrincipal(
                foodie.getUsername(),
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
