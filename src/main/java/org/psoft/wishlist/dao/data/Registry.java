package org.psoft.wishlist.dao.data;

import org.psoft.wishlist.util.TokenGenerator;

public class Registry {

	final int id;
	
	final int ownerId; 

	final String token;
	
	String name;

	public Registry(int id, int ownerId, String token) {
		this.id = id;
		this.ownerId = ownerId;
		this.token = token;
	}

	public Registry() {
		this.id = -1;
		this.ownerId = -1;
		this.token = null;
	}

	public int getId() {
		return id;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getToken() {
		return token;
	}


	
}
