package com.kanpo.trial.exception;

public class InternalServerException extends Exception {
    private String message;

    public InternalServerException(String message){
        this.message = message;
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
