package com.example.AppSaumerios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // ✅ activa el caché
public class AppSaumeriosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppSaumeriosApplication.class, args);
	}

}
