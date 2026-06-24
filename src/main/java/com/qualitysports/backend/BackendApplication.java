package com.qualitysports.backend;

import com.qualitysports.backend.heka.service.HekaShippingService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	ApplicationRunner hekaWarehouseDiag(HekaShippingService heka) {
		return args -> heka.logWarehouses();
	}

}
