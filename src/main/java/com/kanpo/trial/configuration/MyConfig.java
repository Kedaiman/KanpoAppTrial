package com.kanpo.trial.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kanpo.trial.interceptor.MyInterceptor;

@Configuration
public class MyConfig implements WebMvcConfigurer {

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

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(myInterceptor());
	}
}
