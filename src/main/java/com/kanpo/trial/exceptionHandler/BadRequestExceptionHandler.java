package com.kanpo.trial.exceptionHandler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.kanpo.trial.exception.BadRequestException;
import com.kanpo.trial.exception.InternalServerException;
import com.kanpo.trial.restResponse.ErrorResponse;

/**
* 例外が発生した際のハンドラークラス
* @author　keita
*/
@ControllerAdvice
public class BadRequestExceptionHandler {

	/**
	 * BadRequestException例外を処理するハンドラーメソッド
	 *
	 * @param req requestオブジェクト
	 * @param e BadRequestException例外オブジェクト
	 * @return エラー発生時のレスポンス内容オブジェクトを返却
	 */
	// BadRequestExceptionを処理するハンドラー
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> getException(HttpServletRequest req, BadRequestException e){
        return ErrorResponse.createResponse(e);
    }

	/**
	 * InternalServerException例外を処理するハンドラーメソッド
	 *
	 * @param req requestオブジェクト
	 * @param e InternalServerException例外オブジェクト
	 * @return エラー発生時のレスポンス内容オブジェクトを返却
	 */
    // InternalServerExceptionを処理するハンドラー
    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> getException(HttpServletRequest req, InternalServerException e){
        return ErrorResponse.createResponse(e);
    }

	/**
	 * その他例外を処理するハンドラーメソッド
	 *
	 * @param req requestオブジェクト
	 * @return エラー発生時のレスポンス内容オブジェクトを返却
	 */
    // その他の例外を処理するハンドラー
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> getException(HttpServletRequest req) {
    	// InternalServerExceptionに倒す
    	return ErrorResponse.createResponse(new InternalServerException("unknownerror"));
    }
}