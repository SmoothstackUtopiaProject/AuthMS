package com.utopia.auth.exceptions;

public class PasswordNotAllowedException extends Exception {

	  private static final long serialVersionUID = 1L;

	  public PasswordNotAllowedException() {}

	  public PasswordNotAllowedException(String message) {
	    super(message);
	  }
	}
