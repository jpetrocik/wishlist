package org.psoft.wishlist.dao.data;

public class WishlistUser {

	String initials;
	
	String email;

	public WishlistUser(String initials, String email) {
		this.initials = initials;
		this.email = email;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
