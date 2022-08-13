package com.kanpo.trial.log;
import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyLogger {
	private static Logger logger = null;

	public static void init() {
		logger = LoggerFactory.getLogger("SYSTEM");
	}

	public static void info(String message) {
		logger.info(message);
	}

	public static void info(String message, String... args) {
		logger.info(MessageFormat.format(message, args));
	}

	public static void info(String message, Object... args) {
		logger.info(MessageFormat.format(message, args));
	}

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

	public static void debug(String message) {
		logger.debug(message);
	}

	public static void debug(String message, String... args) {
		logger.debug(MessageFormat.format(message, args));
	}

	public static void error(String message) {
		logger.error(message);
	}

	public static void error(String message, String... args) {
		logger.error(message.formatted(message, args));
	}

	public static void error(Exception e) {
		logger.error(e.getMessage(), e);
	}
}
