package com.utopia.auth.exceptions;

public class IncorrectPasswordException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public IncorrectPasswordException() {};
	
	public IncorrectPasswordException(String message) {
		super(message);
	}

	
}
