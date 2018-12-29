package org.psoft.wishlist.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.psoft.wishlist.dao.RegistryDao;
import org.psoft.wishlist.dao.data.Account;
import org.psoft.wishlist.dao.data.Invitation;
import org.psoft.wishlist.dao.data.Registry;
import org.psoft.wishlist.dao.data.RegistryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistryService {
	@Autowired
	RegistryDao wishListDao;

	@Autowired
	AccountService accountService;

	@Autowired
	InvitationService invitationService;

	@Autowired
	EmailerService emailer;

	public Invitation startNewRegistry(String email) {
		Account account = accountService.findByEmail(email);
		if (account == null) {
			account = accountService.register(email);
		}

		Invitation invitation = createRegistry(account, account.getName());
		return invitation;
	}

	public Invitation createRegistry(int ownerId, String name) {
		Account owner = accountService.findById(ownerId);
		if (owner == null) {
			return null;
		}

		return createRegistry(owner, name);

	}

	public Invitation createRegistry(Account owner, String name) {
		Registry registry = wishListDao.createRegistry(name, owner.getId());

		//create invitation for owner
		Invitation invitation = invitationService.createInvitation(registry.getToken(), owner.getEmail());

		return invitation;
	}

	public Registry updateRegistry(int ownerId, int registryId, Registry registry) {
		return wishListDao.updateRegistry(ownerId, registryId, registry);
	}

//	public Registry findDefaultRegistery(int userId) {
//		return wishListDao.defaultRegistry(userId);
//	}

	public Registry registry(int registryId) {
		return wishListDao.registry(registryId);
	}

	public Registry registryByToken(String token) {
		return wishListDao.registryByToken(token);
	}

	public List<RegistryItem> registryItems(int registryId) {
		return wishListDao.registryItems(registryId);
	}

	public RegistryItem registryItem(int giftId) {
		return wishListDao.registryItem(giftId);
	}

	public RegistryItem addRegistryItem(int registryId, int userId, RegistryItem registryItem) {
		boolean isOwner = isOwner(userId, registryId);
		registryItem.setSecret(!isOwner);

		return wishListDao.createRegsitryItem(registryId, userId, registryItem);
	}

	public RegistryItem updateRegistryItem(int ownerId, int registryItemId, RegistryItem registryItem) {
		return wishListDao.updateRegistryItem(ownerId, registryItemId, registryItem);
	}

	public void deleteRegistryItem(int ownerId, int registryItemId) {
		wishListDao.deleteRegistryItem(ownerId, registryItemId);
	}

	public void purchasedRegistryItem(int registryItemId, String name) {
		wishListDao.purchasedRegistryItem(registryItemId, name);
	}


	public boolean isOwner(int userId, int registryId){
		Registry registry = registry(registryId);
		return registry.getOwnerId() == userId;
	}

	public String createGroup(String token, int ownerId, String[] emails) {

		//create registry and add to group
		for (String e: emails){
			Account wishListUser = accountService.findByEmail(e);
			if (wishListUser == null) {
				wishListUser = accountService.register(e);
			}

			Invitation invitation = startNewRegistry(e);
			Registry registry = registryByToken(invitation.getTokenToAccess());

			wishListDao.createGroup(token, registry.getId());

			sendGroupInvitation(e, token);
		}

		//Create invitation to the existing registry in the group and add
		//invitations to the joining users
		List<Registry> allGroupRegistries = groupRegistries(token);
		Set<Integer> groupMembers = allGroupRegistries.stream().map(r->r.getOwnerId()).collect(Collectors.toSet());
		Set<String> groupRegistries = allGroupRegistries.stream().map(r->r.getToken()).collect(Collectors.toSet());

		//add this member to all group registry and
		//add all the group member to this register
		for (Integer userId : groupMembers) {
			for (String registryTokens  : groupRegistries) {
				invitationService.createInvitation(registryTokens, userId);
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

	public void sendInvitation(String email, String token) {
		Account account = accountService.findByEmail(email);
		String authorizationToken = accountService.lookupUserAuthToken(account.getId());
		sendInvitationEmail(account.getEmail(), "Wish List Invitation",
			"Use this link to access the your wish list.\n\nhttp://gifts.petrocik.net/#/" + token + "?authorizationToken=" + authorizationToken);
	}

	public void sendGroupInvitation(String email, String token) {
		Account account = accountService.findByEmail(email);
		String authorizationToken = accountService.lookupUserAuthToken(account.getId());
		sendInvitationEmail(account.getEmail(), token + " Wish List Invitation",
			"Use this link to access the " + token + " Wish List.\n\nhttp://gifts.petrocik.net/#/" + token + "?authorizationToken=" + authorizationToken);
	}

	private void sendInvitationEmail(String email, String subject, String body) {
		try {
			emailer.sendMail(email, "no-reply@petrocik.net", subject, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
