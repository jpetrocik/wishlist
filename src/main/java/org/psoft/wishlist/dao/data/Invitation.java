package org.psoft.wishlist.dao.data;


public class Invitation {

	final int id;

	final String tokenToAccess;

	final int invitedUserId;

	final String token;

	public Invitation(int id, String tokenToAccess, int invitedUserId, String token) {
		this.id = id;
		this.tokenToAccess = tokenToAccess;
		this.invitedUserId = invitedUserId;
		this.token = token;
	}

	public int getId() {
		return id;
	}

	public String getTokenToAccess() {
		return tokenToAccess;
	}

	public int getInvitedUserId() {
		return invitedUserId;
	}

	public String getToken() {
		return token;
	}


}
