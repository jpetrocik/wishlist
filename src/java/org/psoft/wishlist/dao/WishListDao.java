package org.psoft.wishlist.dao;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.psoft.wishlist.torque.Gift;
import org.psoft.wishlist.torque.GiftPeer;

public class WishListDao {
	private static Log log = LogFactory.getLog(WishListDao.class);

	public List fetchWishList(String name) {
		List wishList;

		try {
			Criteria crit = new Criteria();
			crit.add(GiftPeer.INTIALS, name);
			wishList = GiftPeer.doSelect(crit);
		} catch (TorqueException te) {
			String msg = "Unable to fetch wish list for " + name;
			log.error(msg, te);
			throw new RuntimeException(msg, te);
		}

		return wishList;
	}

	public void save(Gift gift) {
		try {
			gift.save();
		} catch (Exception e) {
			String msg = "Unable to save gift " + gift.getTitle();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	public void save(List wishList) {

		for (Iterator iter = wishList.iterator(); iter.hasNext(); ){
			Gift gift = (Gift)iter.next();
			
			try {
				gift.save();
			} catch (Exception e) {
				log.error("Continuing to save wish list after exception");
			}
		}
	}
}
