package com.kanpo.trial.restResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.kanpo.trial.exception.BadRequestException;
import com.kanpo.trial.exception.InternalServerException;

public class ErrorResponse {
    private String Message;

    public ErrorResponse(String  message){
        this.Message = message;
    }

    public static ResponseEntity<ErrorResponse> createResponse(BadRequestException e){
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    public static ResponseEntity<ErrorResponse> createResponse(InternalServerException e) {
    	return new ResponseEntity<ErrorResponse>(
    			new ErrorResponse(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

	public String getMessage() {
		return Message;
	}

	public void setMessage(String message) {
		Message = message;
	}
}