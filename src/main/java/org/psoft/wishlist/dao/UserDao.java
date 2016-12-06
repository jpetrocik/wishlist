package org.psoft.wishlist.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.psoft.wishlist.dao.data.WishlistUser;
import org.springframework.stereotype.Component;

@Component
public class UserDao {

	Map<String, WishlistUser> allUsers = new HashMap<>();

	public UserDao() {
		addUser(new WishlistUser("JMP", "john@petrocik.net"));
		addUser(new WishlistUser("NTP", "nanelle@petrocik.net"));
		addUser(new WishlistUser("MKP", "marhiyana@petrocik.net"));
		addUser(new WishlistUser("CDP", "cadel@petrocik.net"));
		addUser(new WishlistUser("JRP", "jrp@petrocik.net"));
		addUser(new WishlistUser("MAP", "map@petrocik.net"));
		addUser(new WishlistUser("EXF", "exferrara@gmail.com"));
		addUser(new WishlistUser("SLF", null));
		addUser(new WishlistUser("JSF", null));
		addUser(new WishlistUser("MEF", null));
		addUser(new WishlistUser("MLG", null));
	}

	private void addUser(WishlistUser wishlistUser) {
		allUsers.put(wishlistUser.getInitials(), wishlistUser);
	}
	public WishlistUser findByInitials(String initials){
		return allUsers.get(initials);
	}
	
	public Collection<WishlistUser> findAll(){
		return allUsers.values();
	}
}
