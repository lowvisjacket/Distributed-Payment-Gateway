package com.example.distributed_payment_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DistributedPaymentGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributedPaymentGatewayApplication.class, args);
	}

}
