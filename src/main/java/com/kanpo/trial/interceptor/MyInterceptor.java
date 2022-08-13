package com.kanpo.trial.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.kanpo.trial.log.MyLogger;

public class MyInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request,
	    HttpServletResponse response, Object handler) throws Exception {
		MyLogger.info("[START] Path={0} Method={1}", request.getServletPath(), request.getMethod());
		// リクエストパラメーターが存在すればログに出力
		MyLogger.info(request.getParameterMap());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response,
	    Object handler, ModelAndView modelAndView) throws Exception {
		MyLogger.info("[END] Path={0} Method={1}", request.getServletPath(), request.getMethod());
		// リクエストパラメーターが存在すればログに出力
	}
}
