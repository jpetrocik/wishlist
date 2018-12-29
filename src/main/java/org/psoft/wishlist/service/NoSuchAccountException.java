package org.psoft.wishlist.service;

@SuppressWarnings("serial")
public class NoSuchAccountException extends RuntimeException {

	String email;

	public NoSuchAccountException(String email) {
		this.email = email;
	}

	@Override
	public String getMessage() {
		return "No such account exists for " + email;
	}
}
