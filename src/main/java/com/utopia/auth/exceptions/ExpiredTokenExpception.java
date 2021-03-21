package com.utopia.auth.exceptions;

public class ExpiredTokenExpception extends Exception {

	  public ExpiredTokenExpception() {}

	  private static final long serialVersionUID = 1L;

	  public ExpiredTokenExpception(String message) {
	    super(message);
	  }
	}
