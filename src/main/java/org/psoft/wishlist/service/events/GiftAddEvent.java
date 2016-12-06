package org.psoft.wishlist.service.events;

public class GiftAddEvent {
	
	public String initials;
	
	public String title;
	
	public String toString() {
		return initials + " added \"" + title + "\"";
	}
}
