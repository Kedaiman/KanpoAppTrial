package com.kanpo.trial;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.kanpo.trial.log.MyLogger;

/**
* アプリケーションのmainクラス
* @author　keita
*/
@SpringBootApplication
@EnableScheduling
public class KanpoAppTrialApplication {
	/**
	 * アプリケーションのmainメソッド
	 *
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {
		// ログを初期化
		MyLogger.init();
		// サービス起動
		SpringApplication.run(KanpoAppTrialApplication.class, args);
	}

}
