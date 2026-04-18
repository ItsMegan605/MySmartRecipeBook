package it.unipi.MySmartRecipeBook.utils;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// TODO: si può togliere tanto noi la abbiamo per l'inserimento
/**
 * Class to generate the hash for the admin's password
 */
public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("admin"));
    }
}

