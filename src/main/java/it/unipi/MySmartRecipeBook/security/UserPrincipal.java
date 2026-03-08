package it.unipi.MySmartRecipeBook.security;

import it.unipi.MySmartRecipeBook.model.Mongo.users.Chef;
import it.unipi.MySmartRecipeBook.model.Mongo.users.Foodie;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
//rappresentazione dell'utente autentixato all'interno di Spring Security,
// implementa interfacia UserDetails, richieesta per gestire autenticazione e autorizzazione


//ogni volta che l'utente effettua il login, spring secuirty crea un oggetto UserPrincipal.
//con le info dell'utente e il suo ruolo
public class UserPrincipal implements UserDetails {

    //password dell'utente (utilizzata per verificare le credenziali durante il login)
    private String password;
    // identificativo univoco dell'utente nel database
    private String id;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

    //costruttore
    public UserPrincipal(String id, String username, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    //Chef
    /* Metodo factory per creare un UserPrincipal a partire da un oggetto Chef,
     * Viene assegnato automaticamente il ruolo ROLE_CHEF
     */
    public static UserPrincipal buildChef(Chef chef) {
        return new UserPrincipal(
                chef.getId(),
                chef.getUsername(),
                chef.getPassword(),
                Collections.singletonList (new SimpleGrantedAuthority("ROLE_CHEF"))
        );
    }

    //Foodie - same thing
    public static UserPrincipal buildFoodie(Foodie foodie) {
        return new UserPrincipal(
                foodie.getId(),
                foodie.getUsername(),
                foodie.getPassword(),
                Collections.singletonList (new SimpleGrantedAuthority("ROLE_FOODIE"))
        );
    }

    //Admin
    public static UserPrincipal buildAdmin(Chef admin) {
        return new UserPrincipal(
                admin.getId(),
                admin.getUsername(),
                admin.getPassword(),
                Collections.singletonList (new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    /* Restituisce i ruoli associati all'utente, Spring Security utilizza queste informazioni per
     verificare se l'utente ha i permessi necessari per accedere a un endpoint.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /* Restituisce la password dell'utente.
     * Utilizzata dal sistema di autenticazione per confrontare
     * la password inserita con quella salvata nel database.
     */
    @Override
    public String getPassword() {
        return password;
    }


    /* Restituisce l'id dell'utente.
     * Non fa parte dell'interfaccia UserDetails ma è utile
     * per identificare l'utente autenticato nelle operazioni applicative.
     */
    public String getId() {
        return id;
    }

    /* Restituisce lo username utilizzato per il login.
     * Questo metodo è richiesto dall'interfaccia UserDetails.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /* I seguenti metodi indicano lo stato dell'account.
     * In questo progetto tutti gli account sono considerati validi,
     * quindi restituiscono sempre true.
     */
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
