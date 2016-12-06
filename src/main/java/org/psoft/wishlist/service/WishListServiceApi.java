package org.psoft.wishlist.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.psoft.wishlist.dao.WishListDao;
import org.psoft.wishlist.dao.data.Gift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WishListServiceApi {

	@Autowired
	WishListDao wishListDao;

	/**
	 * Returns active users wishlist, secret items are removed
	 */
	@RequestMapping(path="/api/id", method=RequestMethod.GET)
	public String index(HttpSession session){
		String user = (String) session.getAttribute("user");
		return user;
	}

	/**
	 * Returns request wishlist, if active users wishlist secret items are removed
	 */
	@RequestMapping(path="/api/{initials}/", method=RequestMethod.GET)
	public List<Gift> retrieve(@PathVariable String initials, HttpSession session){
		String user = (String) session.getAttribute("user");
		List<Gift> wishlist = wishListDao.fetch(initials);
		
		//filter secret gifts from logged in user
		if (initials.equals(user)) {
			List<Gift> wishList = wishlist.stream().filter(g -> !g.isSecret()).collect(Collectors.toList());
			wishList.forEach(g -> g.setPurchased(false));
			return wishList;
		}
		return wishListDao.fetch(initials);
	}
	
	@RequestMapping(path="/api/{initials}/", method=RequestMethod.PUT)
	public Gift update(@PathVariable String initials, @RequestBody Gift gift, HttpSession session){
		String user = (String) session.getAttribute("user");
		if (!initials.equals(user)){
			gift.setSecret(true);
		}
		gift.setInitials(initials);
		
		return wishListDao.save(gift);
		
	}
	
	@RequestMapping(path="/api/{initials}/", method=RequestMethod.POST)
	public Gift save(@PathVariable String initials, @RequestBody Gift gift, HttpSession session){
		gift = wishListDao.save(gift);
		return gift;
	}

    @RequestMapping("/api/{giftId}/purchased")
    public String purchasedToggle(@PathVariable Integer giftId, HttpSession session){
		String user = (String) session.getAttribute("user");
		int updated = wishListDao.purchased(giftId, user);
		if (updated == 0) {
			throw new RuntimeException("Update failed");
		}
		
        return user;
	}
	
}
