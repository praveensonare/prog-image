package com.prog.image;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProgImageApp {

	public static void main(String[] args) {
		SpringApplication.run(ProgImageApp.class, args);
	}
	private SecurityScheme createAPIKeyScheme() {
		return new SecurityScheme().type(SecurityScheme.Type.HTTP)
				.bearerFormat("JWT")
				.scheme("bearer");
	}

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI().addSecurityItem(new SecurityRequirement().
						addList("Bearer Authentication"))
				.components(new Components().addSecuritySchemes
						("Bearer Authentication", createAPIKeyScheme()))
				.info(new Info().title("Prog Image")
						.description("Image Editor Utility")
						.version("1.0").contact(new Contact().name("Praveen Sonare")
								.email( "praveensonare007@gmail.com.com"))
						.license(new License().name("License of API")
								.url("API license URL")));
	}
}
