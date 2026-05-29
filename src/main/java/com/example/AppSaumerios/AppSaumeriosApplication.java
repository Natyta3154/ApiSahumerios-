package com.example.AppSaumerios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableCaching // ✅ activa el caché
@EnableAsync   // ✅ habilita tareas asíncronas
public class AppSaumeriosApplication {

	@Value("${spring.mail.username:No se leyó}")
	private String mailUser;

	public static void main(String[] args) {
		SpringApplication.run(AppSaumeriosApplication.class, args);
	}

	@PostConstruct
	public void verificarEnv() {
		System.out.println("📧 Configuración Gmail detectada: " + mailUser);
	}
}
