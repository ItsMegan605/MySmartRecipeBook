package it.unipi.MySmartRecipeBook.utils;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Class to generate the hash for the admin's password
 */
public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("admin"));
    }
}

