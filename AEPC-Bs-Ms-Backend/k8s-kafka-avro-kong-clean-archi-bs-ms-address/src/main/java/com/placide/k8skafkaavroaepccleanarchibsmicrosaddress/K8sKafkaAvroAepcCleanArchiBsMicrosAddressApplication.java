package com.placide.k8skafkaavroaepccleanarchibsmicrosaddress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class K8sKafkaAvroAepcCleanArchiBsMicrosAddressApplication {
	public static void main(String[] args) {
		SpringApplication.run(K8sKafkaAvroAepcCleanArchiBsMicrosAddressApplication.class, args);
	}

}
