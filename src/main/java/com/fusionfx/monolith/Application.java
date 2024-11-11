package com.fusionfx.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
@EnableConfigurationProperties
public class Application {

	public static void main(final String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
