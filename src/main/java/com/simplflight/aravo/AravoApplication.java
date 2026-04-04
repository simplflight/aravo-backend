package com.simplflight.aravo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AravoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AravoApplication.class, args);
	}

}
