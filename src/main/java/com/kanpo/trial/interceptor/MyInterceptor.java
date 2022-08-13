package com.kanpo.trial.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.kanpo.trial.log.MyLogger;
/**
* Interceptorクラス
* @author　keita
*/
public class MyInterceptor implements HandlerInterceptor {

	/**
	 * API処理実行の前処理
	 *
	 * @param request リクエスト
	 * @param response レスポンス
	 * @param handler ハンドラー
	 * @return API処理実行する場合はtrue
	 * @throws Exception 例外送出
	 */
	@Override
	public boolean preHandle(HttpServletRequest request,
	    HttpServletResponse response, Object handler) throws Exception {
		MyLogger.info("[START] Path={0} Method={1}", request.getServletPath(), request.getMethod());
		// リクエストパラメーターが存在すればログに出力
		MyLogger.info(request.getParameterMap());
		return true;
	}

	/**
	 * API処理実行の後処理
	 *
	 * @param request リクエスト
	 * @param response レスポンス
	 * @param handler ハンドラー
	 * @param modelAndView 使わない(REST-APIのみの提供のため)
	 * @throws Exception 例外送出
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response,
	    Object handler, ModelAndView modelAndView) throws Exception {
		MyLogger.info("[END] Path={0} Method={1}", request.getServletPath(), request.getMethod());
		// リクエストパラメーターが存在すればログに出力
	}
}
