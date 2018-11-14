package org.psoft.wishlist.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.psoft.wishlist.dao.UserDao;
import org.psoft.wishlist.dao.WishListDao;
import org.psoft.wishlist.dao.data.Invitation;
import org.psoft.wishlist.dao.data.Registry;
import org.psoft.wishlist.dao.data.RegistryItem;
import org.psoft.wishlist.dao.data.WishlistUser;
import org.psoft.wishlist.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

@Component
public class RegistryService {
	@Autowired
	EventBus eventBus;
	
	@Autowired
	WishListDao wishListDao;

	@Autowired
	UserDao userDao;

	public WishlistUser register(String email, String name) {
		return userDao.register(email, name);
	}

	public Invitation startNewRegistry(String email) {
		WishlistUser wishListUser = userDao.findByEmail(email);
		if (wishListUser == null) {
			wishListUser = register(email, StringUtils.substringBefore(email, "@"));
		}
		
		Invitation invitation = createRegistry(wishListUser, wishListUser.getName());
		return invitation;
	}

	public Invitation createRegistry(int ownerId, String name) {
		WishlistUser owner = userDao.findById(ownerId);
		if (owner == null) {
			return null;
		}
		
		return createRegistry(owner, name);
			
	}
		
	public Invitation createRegistry(WishlistUser owner, String name) {
		Registry registry = wishListDao.createRegistry(name, owner.getId());
		
		//create invitation for owner
		Invitation invitation = createInvitation(registry.getId(), owner.getEmail());
		
		return invitation;
	}

	public Invitation createInvitation(int registryId, String email) {
		WishlistUser invitedUser = userDao.findByEmail(email);
		if (invitedUser == null) {
			invitedUser = register(email, StringUtils.substringBefore(email, "@"));
		}
		
		Invitation invitaiton = wishListDao.createInvitation(registryId, invitedUser.getId());
		
		sendInvitationEmail(invitaiton);
		
		return invitaiton;

	}

	private void sendInvitationEmail(Invitation invitaiton) {
		System.out.println("/invitation/" + invitaiton.getToken());
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
		if (isOwner) {
			registryItem.setSecret(true);
		}
		
		return wishListDao.createRegsitryItem(registryId, userId, registryItem);
	}
	
	public RegistryItem updateRegistryItem(int registryId, int userId, RegistryItem registryItem) {
		return wishListDao.updateRegistryItem(registryId, userId, registryItem);
	}

	public boolean isOwner(int registryId, int userId){
		Registry registry = registry(registryId);
		
		return registry.getOwnerId() == userId;
	}

	public boolean hasInvitation(int registryId, int userId) {
		Invitation invitation = wishListDao.invitation(registryId, userId);
		return invitation != null;
	}

	public WishlistUser wishListUser(int id) {
		return userDao.findById(id);
	}


}
