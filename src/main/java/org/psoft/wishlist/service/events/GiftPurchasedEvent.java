package org.psoft.wishlist.service.events;

public class GiftPurchasedEvent {

	public String initials;
	
	public String title;
	
	public String purchasedBy;
	
	public String toString() {
		return purchasedBy + " purchased \"" + title + "\" for " + initials;
	}

}
