package com.kanpo.trial.restResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.kanpo.trial.exception.BadRequestException;

public class ErrorResponse {
    private String Message;

    public ErrorResponse(String  message){
        this.Message = message;
    }

    public static ResponseEntity<ErrorResponse> createResponse(BadRequestException e){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}
}