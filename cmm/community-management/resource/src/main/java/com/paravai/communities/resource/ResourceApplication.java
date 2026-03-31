package com.paravai.communities.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.paravai.communities.resource",
        "com.paravai.foundation.viewjsonapi.exception",
		"com.paravai.foundation.localization",
		"com.paravai.foundation.infrastructure.event",
		"com.paravai.foundation.infrastructure.kafka",
        "com.paravai.foundation.integration"

})
public class ResourceApplication {

	public static void main(String[] args) {

		Logger log = LoggerFactory.getLogger(ResourceApplication.class);
		log.info("PRUEBA DE LOG ACTIVA");
		SpringApplication.run(ResourceApplication.class, args);
	}

    @Bean
    public ApplicationRunner checkBeans(ApplicationContext ctx) {
        return args -> {
            System.out.println("--------------------------------------------------");
            System.out.println("Beans of type MeterRegistry:");
            ctx.getBeansOfType(io.micrometer.core.instrument.MeterRegistry.class)
                    .forEach((name, bean) -> System.out.println(" - " + name + " : " + bean.getClass().getName()));
            System.out.println("Beans of type WebClient:");
            ctx.getBeansOfType(org.springframework.web.reactive.function.client.WebClient.class)
                    .forEach((name, bean) -> System.out.println(" - " + name));
            System.out.println("--------------------------------------------------");
        };
    }




}
