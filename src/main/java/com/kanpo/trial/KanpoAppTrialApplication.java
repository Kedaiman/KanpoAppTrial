package com.kanpo.trial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.kanpo.trial.log.MyLogger;

@SpringBootApplication
@EnableScheduling
public class KanpoAppTrialApplication {

	public static void main(String[] args) {
		// ログを初期化
		MyLogger.init();
		// サービス起動
		SpringApplication.run(KanpoAppTrialApplication.class, args);
	}

}
