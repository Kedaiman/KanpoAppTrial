package com.kanpo.trial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KanpoAppTrialApplication {

	public static void main(String[] args) {
		SpringApplication.run(KanpoAppTrialApplication.class, args);
	}

}
