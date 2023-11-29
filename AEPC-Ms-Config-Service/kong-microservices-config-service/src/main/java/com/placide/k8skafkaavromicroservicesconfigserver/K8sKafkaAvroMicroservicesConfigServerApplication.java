package com.placide.k8skafkaavromicroservicesconfigserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class K8sKafkaAvroMicroservicesConfigServerApplication {
	public static void main(String[] args) throws InterruptedException {
		Thread.sleep(10_000);
		new SpringApplication(K8sKafkaAvroMicroservicesConfigServerApplication.class)
				.run(args);
	}
}
