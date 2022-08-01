package com.kanpo.trial.exceptionHandler;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.kanpo.trial.exception.BadRequestException;
import com.kanpo.trial.restResponse.ErrorResponse;

@ControllerAdvice
public class BadRequestExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> getException(HttpServletRequest req, BadRequestException e){
        return ErrorResponse.createResponse(e);
    }
}