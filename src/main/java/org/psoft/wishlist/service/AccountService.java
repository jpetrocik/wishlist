package org.psoft.wishlist.service;

import javax.servlet.http.HttpSession;

import org.psoft.wishlist.dao.UserDao;
import org.psoft.wishlist.dao.data.WishlistUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.plivo.api.PlivoClient;

@Component
public class AccountService {

	@Autowired
	UserDao accountDao;

	@Autowired
	PlivoClient plivoClient;

	public String sendMFAMessage(String phone) throws Exception {
		return accountDao.generateMFAToken(phone);
	}

	public String validatedMFAMessage(String token, String code) {
		return accountDao.validateMFAToken(token, code);
	}

	public void authenicateUser(String authenticationToken, HttpSession session) {
		WishlistUser wishListUser = accountDao.validateAuthtoken(authenticationToken);

		authenicateUser(wishListUser, session);
	}

	public void authenicateUser(int accountId, HttpSession session) {
		WishlistUser wishListUser = accountDao.findById(accountId);

		authenicateUser(wishListUser, session);
	}

	private void authenicateUser(WishlistUser wishListUser, HttpSession session) {
		session.setAttribute("user", wishListUser);
	}

}
