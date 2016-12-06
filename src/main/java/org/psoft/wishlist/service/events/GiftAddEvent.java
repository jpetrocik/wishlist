package org.psoft.wishlist.service.events;

import org.psoft.wishlist.dao.data.Gift;

public class GiftAddEvent {
	
	public String who;
	
	public Gift gift;
	
	public String toString() {
		return who + " added \"" + gift.getTitle() + "\"" + (gift.isSecret()?" for " + gift.getInitials():"");
	}
}
