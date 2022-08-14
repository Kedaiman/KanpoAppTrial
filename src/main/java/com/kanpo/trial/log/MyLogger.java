package com.kanpo.trial.log;
import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* ログ出力用クラス
* @author　keita
*/
public class MyLogger {
	/** loggerフィールド */
	private static Logger logger = null;

	/**
	 * ログ初期化メソッド
	 */
	public static void init() {
		logger = LoggerFactory.getLogger("SYSTEM");
	}

	/**
	 * ログ初期化メソッド
	 * @param message ログメッセージ
	 */
	public static void info(String message) {
		logger.info(message);
	}

	/**
	 * INFOログ出力
	 * @param message ログメッセージ
	 * @param args 置換用データ(可変長引数)
	 */
	public static void info(String message, Object... args) {
		logger.info(MessageFormat.format(message, args));
	}

	/**
	 * bodyの入力パラメーター INFOログ出力
	 * @param paramMap 入力データマップ <key, value>
	 */
	public static void info(Map<String, String[]> paramMap) {
		String message = "query: ";
		if (paramMap.size() > 0) {
			for (String key : paramMap.keySet()) {
				//message += MessageFormat.format("{0} = {1}, ", key, Arrays.toString(paramMap.get(key)));
				message += MessageFormat.format("{0} = {1}, ", key, paramMap.get(key)[0]);
			}
			MyLogger.info(message);
		}
		return;
	}

	/**
	 * DEBUGログ出力
	 * @param message メッセージ
	 */
	public static void debug(String message) {
		logger.debug(message);
	}

	/**
	 * DEBUGログ出力
	 * @param message ログメッセージ
	 * @param args 置換用データ(可変長引数)
	 */
	public static void debug(String message, Object... args) {
		logger.debug(MessageFormat.format(message, args));
	}

	/**
	 * ERRORログ出力
	 * @param message メッセージ
	 */
	public static void error(String message) {
		logger.error(message);
	}

	/**
	 * ERRORログ出力
	 * @param message ログメッセージ
	 * @param args 置換用データ(可変長引数)
	 */
	public static void error(String message, Object... args) {
		logger.error(MessageFormat.format(message, args));
	}

	/**
	 * ERRORログ出力 (例外情報出力)
	 * @param e 例外オブジェクト
	 */
	public static void error(Exception e) {
		logger.error(e.getMessage(), e);
	}
}
