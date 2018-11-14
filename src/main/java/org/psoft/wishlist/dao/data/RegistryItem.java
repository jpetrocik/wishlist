package org.psoft.wishlist.dao.data;

import org.apache.commons.lang3.StringUtils;

public class RegistryItem {

	final int id;

	final int ownerId; 
	
	final int registryId;
	
	String url;

	String descr;

	boolean purchased;

	String purchasedBy;

	boolean secret;

	public RegistryItem(int id, int ownerId, int registryId) {
		this.id = id;
		this.ownerId = ownerId;
		this.registryId = registryId;
	}
	
	public RegistryItem() {
		this.id = -1;
		this.ownerId = -1;
		this.registryId = -1;
	}
	
	public int getId() {
		return id;
	}

	public String getUrl() {
		return StringUtils.trimToNull(url);
	}

	public void setUrl(String v) {
		this.url = v;
	}

	public String getDescr() {
		return StringUtils.trimToNull(descr);
	}

	public void setDescr(String v) {
		this.descr = v;
	}

	public boolean isPurchased() {
		return purchased;
	}

	public void setPurchased(boolean v) {
			this.purchased = v;
	}

	public String getPurchasedBy() {
		return purchasedBy;
	}

	public void setPurchasedBy(String v) {
		this.purchasedBy = v;
	}

	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean v) {
		this.secret = v;
	}

	public int getRegistryId() {
		return registryId;
	}

	public int getOwnerId() {
		return ownerId;
	}
}
