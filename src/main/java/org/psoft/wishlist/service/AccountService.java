package org.psoft.wishlist.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.psoft.wishlist.dao.AccountDao;
import org.psoft.wishlist.dao.data.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountService {

	@Autowired
	AccountDao accountDao;

	public Account register(String email) {
		String name = StringUtils.substringBefore(email, "@");
		return accountDao.register(email, name);
	}

	public Account register(String email, String name) {
		return accountDao.register(email, name);
	}

	public String sendMFAMessage(String phone) throws Exception {
		return accountDao.generateMFAToken(
				cleanPhone(phone));
	}

	public String validatedMFAMessage(String token, String code) {
		return accountDao.validateMFAToken(token, code);
	}

	public void authenicateUser(String authenticationToken, HttpSession session) {
		Account wishListUser = accountDao.validateAuthtoken(authenticationToken);

		authenicateUser(wishListUser, session);
	}

	public void authenicateUser(int accountId, HttpSession session) {
		Account wishListUser = accountDao.findById(accountId);

		authenicateUser(wishListUser, session);
	}

	private void authenicateUser(Account wishListUser, HttpSession session) {
		session.setAttribute("user", wishListUser);
	}

	String cleanPhone(String rawPhone) {
		StringBuilder phone = new StringBuilder();
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(rawPhone);
		while (m.find()) {
		  phone.append(m.group());
		}

		if (phone.charAt(0) != '1') {
			phone.insert(0, '1');
		}

		return phone.toString();
	}

}
