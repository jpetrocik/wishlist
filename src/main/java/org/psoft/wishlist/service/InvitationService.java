package org.psoft.wishlist.service;

import org.psoft.wishlist.dao.InvitationDao;
import org.psoft.wishlist.dao.data.Account;
import org.psoft.wishlist.dao.data.Invitation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InvitationService {

	@Autowired
	AccountService accountService;

	@Autowired
	InvitationDao invitationDao;


	public boolean hasInvitation(int userId, String tokenToAccess) {
		Invitation invitation = invitationDao.invitation(tokenToAccess, userId);
		return invitation != null;
	}

	public Invitation invitation(String token) {
		return invitationDao.invitation(token);
	}

	public Invitation createInvitation(String tokenToAccess, String email) {
		Account invitedUser = accountService.findByEmail(email);
		if (invitedUser == null) {
			throw new NoSuchAccountException(email);
		}

		return createInvitation(tokenToAccess, invitedUser.getId());
	}

	public Invitation createInvitation(String tokenToAccess, int userId) {

		Invitation invitaiton = invitationDao.createInvitation(tokenToAccess, userId);
		return invitaiton;
	}

}
