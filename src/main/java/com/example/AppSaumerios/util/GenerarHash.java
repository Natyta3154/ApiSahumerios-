

// esto es para generar la contraseña del primer admin
package com.example.AppSaumerios.util;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("admin12345"); // la contraseña que quieras
        System.out.println(hash);
    }
}

