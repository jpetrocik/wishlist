package org.psoft.wishlist.dao.data;

import org.psoft.wishlist.util.TokenGenerator;

public class Invitation {

	final int id;
	
	final int registryId;
	
	final int invitedUserId;
	
	final String token;
	
	public Invitation(int id, int registryId, int invitedUserId, String token) {
		this.id = id;
		this.registryId = registryId;
		this.invitedUserId = invitedUserId;
		this.token = token;
	}

	public int getId() {
		return id;
	}

	public int getRegistryId() {
		return registryId;
	}

	public int getInvitedUserId() {
		return invitedUserId;
	}

	public String getToken() {
		return token;
	}


}
