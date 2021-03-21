package com.utopia.auth.exceptions;

public class TokenNotFoundExpection extends Exception {

	private static final long serialVersionUID = 1L;
	
	public TokenNotFoundExpection() {}

	public TokenNotFoundExpection(String message) {
		super(message);
		
	}
	
}
