package org.psoft.wishlist.service.events;

import org.psoft.wishlist.dao.data.Registry;
import org.psoft.wishlist.dao.data.RegistryItem;


public class GiftPurchasedEvent {

	public String who;
	
	public int giftId;
	
	public int registryId;

	public Registry registry;
	
	public RegistryItem registryItem;
	
	public String toMessage() {
		return who + " purchased \"" + registryItem.getDescr() + "\" for " + registry.getOwnerId();
	}

}
