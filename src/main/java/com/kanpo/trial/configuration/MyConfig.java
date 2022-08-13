package com.kanpo.trial.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kanpo.trial.interceptor.MyInterceptor;

/**
* Configurationクラス (Bean登録)
* @author　keita
*/
@Configuration
public class MyConfig implements WebMvcConfigurer {

	/**
	 *  MyInterceptorをBeanとして登録するコンストラクタ
	 *
	 * @return MyInterceptorオブジェクト
	 */
	@Bean
	MyInterceptor myInterceptor() {
		return new MyInterceptor();
	}

	/*
	@Bean
	public CommonsRequestLoggingFilter requestLoggingFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
	    filter.setIncludePayload(true);
	    filter.setMaxPayloadLength(1024);
	    return filter;
	}
	*/

	/**
	 *  MyInterceptorをInterceptorに設定するメソッド
	 *
	 * @param Interceptorに登録するためのレジストリ
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(myInterceptor());
	}
}
