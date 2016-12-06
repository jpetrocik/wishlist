package org.psoft.wishlist.service.events;

import org.psoft.wishlist.dao.data.Gift;

public class GiftPurchasedEvent {

	public String who;
	
	public Gift gift;
	
	public String toString() {
		return who + " purchased \"" + gift.getTitle() + "\" for " + gift.getInitials();
	}

}
