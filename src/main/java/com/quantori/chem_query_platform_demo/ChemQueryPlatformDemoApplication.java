package com.quantori.chem_query_platform_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.quantori.chem_query_platform_demo.repository")
public class ChemQueryPlatformDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChemQueryPlatformDemoApplication.class, args);
	}
}
