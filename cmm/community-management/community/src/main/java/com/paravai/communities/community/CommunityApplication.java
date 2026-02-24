package com.paravai.communities.community;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Locale;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.paravai.communities.community",
        "com.paravai.foundation.viewjsonapi.exception",
		"com.paravai.foundation.localization",
		"com.paravai.foundation.infrastructure.event",
		"com.paravai.foundation.infrastructure.kafka",
        "com.paravai.foundation.integration",

})
public class CommunityApplication {

	public static void main(String[] args) {

		Logger log = LoggerFactory.getLogger(CommunityApplication.class);
		log.info("PRUEBA DE LOG ACTIVA");
		SpringApplication.run(CommunityApplication.class, args);
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

	@Bean
	CommandLineRunner testMessages(MessageSource messageSource) {
		return args -> {
			String message = messageSource.getMessage("community.visibility.PUBLIC", null, Locale.ENGLISH);
			System.out.println("i18n resolved (EN): " + message);
		};
	}



}
