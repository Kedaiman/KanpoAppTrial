package com.kanpo.trial.exceptionHandler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.kanpo.trial.exception.BadRequestException;
import com.kanpo.trial.exception.InternalServerException;
import com.kanpo.trial.restResponse.ErrorResponse;

@ControllerAdvice
public class BadRequestExceptionHandler {

	// BadRequestExceptionを処理するハンドラー
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> getException(HttpServletRequest req, BadRequestException e){
        return ErrorResponse.createResponse(e);
    }

    // InternalServerExceptionを処理するハンドラー
    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> getException(HttpServletRequest req, InternalServerException e){
        return ErrorResponse.createResponse(e);
    }

    // その他の例外を処理するハンドラー
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> getException(HttpServletRequest req) {
    	// InternalServerExceptionに倒す
    	return ErrorResponse.createResponse(new InternalServerException("unknownerror"));
    }
}