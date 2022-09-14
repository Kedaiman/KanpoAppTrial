package com.kanpo.trial.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class MyProperties {

	@Value("${myproperties.cronDeleteMinute}")
	private int cronDeleteMinute;

	public int getCronDeleteMinute() {
		return this.cronDeleteMinute;
	}

	public void setCronDeleteMinute(String cronDeleteMinute) {
		try {
			int localValue = Integer.parseInt(cronDeleteMinute);
			if (localValue < 0) {
				throw new Exception("Negative value specified for cronDeleteMinute");
			}
			this.cronDeleteMinute = localValue;
		} catch (Exception e) {
			// 不正な値が入力されている場合はデフォルト値
			this.cronDeleteMinute = 1;
		}
	}
}
