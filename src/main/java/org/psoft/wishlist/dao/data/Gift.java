package org.psoft.wishlist.dao.data;

import org.apache.commons.lang3.StringUtils;

public class Gift {

	private int giftId;

	private String title;

	private String url;

	private String descr;

	private boolean purchased;

	private String purchasedBy;

	private String initials;

	private boolean secret;

	public int getGiftId() {
		return giftId;
	}

	public void setGiftId(int v) {

		if (this.giftId != v) {
			this.giftId = v;
		}

	}

	public String getTitle() {
		return StringUtils.trimToNull(title);
	}

	public void setTitle(String v) {
		this.title = v;
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

	public String getInitials() {
		return initials;
	}

	public void setInitials(String v) {
		this.initials = v;
	}

	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean v) {
		this.secret = v;
	}

}
