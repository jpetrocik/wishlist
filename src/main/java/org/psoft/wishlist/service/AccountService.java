package org.psoft.wishlist.service;

import org.psoft.wishlist.dao.UserDao;
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
}
