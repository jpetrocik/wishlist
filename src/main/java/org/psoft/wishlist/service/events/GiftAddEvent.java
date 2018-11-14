package org.psoft.wishlist.service.events;

import org.psoft.wishlist.dao.data.RegistryItem;

public class GiftAddEvent {
	
	public String who;
	
	public int giftId;

	public int regsitryId;

//	public String toString() {
//		return who + " added \"" + gift.getDescr() + "\"" + (gift.isSecret()?" for " + gift.getUserId():"");
//	}
}
