package com.example.meter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MeterApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeterApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {

		return new RestTemplate();
	}

//	@Bean
//	public ObjectMapper objectMapper() {
//		return new ObjectMapper();
//	}
}
