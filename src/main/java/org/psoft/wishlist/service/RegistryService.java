package org.psoft.wishlist.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.psoft.wishlist.dao.AccountDao;
import org.psoft.wishlist.dao.RegistryDao;
import org.psoft.wishlist.dao.data.Account;
import org.psoft.wishlist.dao.data.Invitation;
import org.psoft.wishlist.dao.data.Registry;
import org.psoft.wishlist.dao.data.RegistryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
public class RegistryService {
	@Autowired
	RegistryDao wishListDao;

	@Autowired
	AccountService accountService;

	@Autowired
	AccountDao accountDao;

	@Autowired
	EmailerService emailer;

	public Invitation startNewRegistry(String email) {
		Account account = accountDao.findByEmail(email);
		if (account == null) {
			account = accountService.register(email);
		}

		Invitation invitation = createRegistry(account, account.getName());
		return invitation;
	}

	public Invitation createRegistry(int ownerId, String name) {
		Account owner = accountDao.findById(ownerId);
		if (owner == null) {
			return null;
		}

		return createRegistry(owner, name);

	}

	public Registry updateRegistry(int ownerId, int registryId, Registry registry) {
		return wishListDao.updateRegistry(ownerId, registryId, registry);
	}

	public Invitation createRegistry(Account owner, String name) {
		Registry registry = wishListDao.createRegistry(name, owner.getId());

		//create invitation for owner
		Invitation invitation = createInvitation(registry.getId(), owner.getEmail());

		return invitation;
	}

	public Invitation createInvitation(int registryId, String email) {
		Account invitedUser = accountDao.findByEmail(email);
		if (invitedUser == null) {
			invitedUser = accountService.register(email, StringUtils.substringBefore(email, "@"));
		}

		return createInvitation(registryId, invitedUser.getId());
	}

	public Invitation createInvitation(int registryId, int userId) {

		Invitation invitaiton = null;
		try {
			invitaiton = wishListDao.createInvitation(registryId, userId);
		} catch (DuplicateKeyException e){
			invitaiton = wishListDao.invitation(registryId, userId);
		}

		return invitaiton;
	}

	private void sendInvitationEmail(String email, String subject, String body) {
		try {
			emailer.sendMail(email, "no-reply@petrocik.net", subject, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Invitation invitation(String token) {
		return wishListDao.invitation(token);
	}


	public Registry findDefaultRegistery(int userId) {
		return wishListDao.defaultRegistry(userId);
	}

	public Registry registry(int registryId) {
		return wishListDao.registry(registryId);
	}

	public Registry registry(String token) {
		return wishListDao.registry(token);
	}

	public List<RegistryItem> registryItems(int registryId) {
		return wishListDao.registryItems(registryId);
	}

	public RegistryItem addRegistryItem(int registryId, int userId, RegistryItem registryItem) {
		boolean isOwner = isOwner(registryId, userId);
		registryItem.setSecret(!isOwner);

		return wishListDao.createRegsitryItem(registryId, userId, registryItem);
	}

	public RegistryItem updateRegistryItem(int ownerId, int registryItemId, RegistryItem registryItem) {
		return wishListDao.updateRegistryItem(ownerId, registryItemId, registryItem);
	}

	public void purchasedRegistryItem(int registryItemId, String name) {
		wishListDao.purchasedRegistryItem(registryItemId, name);
	}


	public boolean isOwner(int userId, int registryId){
		Registry registry = registry(registryId);

		return registry.getOwnerId() == userId;
	}

	public boolean hasInvitation(int userId, int registryId) {
		Invitation invitation = wishListDao.invitation(registryId, userId);
		return invitation != null;
	}

	public String createGroup(String token, int ownerId, String[] emails) {

		//create registry and add to group
		for (String e: emails){
			Account wishListUser = accountDao.findByEmail(e);
			if (wishListUser == null) {
				wishListUser = accountService.register(e);
			}

			Invitation invitation = startNewRegistry(e);

			//add registry to group
			wishListDao.createGroup(token, invitation.getRegistryId());

			sendInvitation(token, e);
		}

		//Create invitation to the existing registry in the group and add
		//invitations to the joining users
		List<Registry> allGroupRegistries = groupRegistries(token);
		Set<Integer> groupMembers = allGroupRegistries.stream().map(r->r.getOwnerId()).collect(Collectors.toSet());
		Set<Integer> groupRegistries = allGroupRegistries.stream().map(r->r.getId()).collect(Collectors.toSet());

		//add this member to all group registry and
		//add all the group member to this register
		for (Integer userId : groupMembers) {
			for (Integer registryId  : groupRegistries) {
				createInvitation(registryId, userId);
			}
		}

		return token;
	}

	public List<Registry> groupRegistries(String token) {
		List<Registry> registries = new ArrayList<>();

		List<Integer> groupRegistryId = wishListDao.group(token);
		for (Integer registryId : groupRegistryId) {
			registries.add(
					registry(registryId));
		}

		return registries;
	}

	/**
	 * Resends an invitation.
	 *
	 * TODO Use template for email
	 */
	public void resendInvitation(String email, String token) {
		Account account = accountDao.findByEmail(email);
		if (account == null)
			return;

		Invitation invitation = invitation(token);
		if (hasInvitation(account.getId(), invitation.getRegistryId()))
				internalSendInvitation(account, token);
	}

	/**
	 * Sends an invitation.
	 *
	 * TODO Use template for email
	 */
	public void sendInvitation(String email, String token) {
		Account invitedUser = accountDao.findByEmail(email);
		if (invitedUser == null) {
			invitedUser = accountService.register(email, StringUtils.substringBefore(email, "@"));
		}

		internalSendInvitation(invitedUser, token);
	}

	private void internalSendInvitation(Account invitedUser, String token) {
		String authorizationToken = accountDao.generateUserAuthToken(invitedUser.getId());
		sendInvitationEmail(invitedUser.getEmail(), token + " Wish List Invitation",
			"Use this link to access the " + token + " Wish List.\n\nhttp://gifts.petrocik.net/#/" + token + "?authorizationToken=" + authorizationToken);
	}

	public RegistryItem registryItem(int giftId) {
		return wishListDao.registryItem(giftId);
	}
}
