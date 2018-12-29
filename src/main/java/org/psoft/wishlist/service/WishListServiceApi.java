package org.psoft.wishlist.service;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.psoft.wishlist.dao.data.Account;
import org.psoft.wishlist.dao.data.Invitation;
import org.psoft.wishlist.dao.data.Registry;
import org.psoft.wishlist.dao.data.RegistryItem;
import org.psoft.wishlist.service.events.GiftAddEvent;
import org.psoft.wishlist.service.events.GiftPurchasedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.eventbus.EventBus;

@RestController
public class WishListServiceApi {

	@Autowired
	InvitationService invitationService;

	@Autowired
	RegistryService registryService;

	@Autowired
	AccountService accountService;;

	@Autowired
	ScraperService scraperService;

	@Autowired
	EventBus eventBus;

	/**
	 * Registers a new account
	 */
	@RequestMapping(path="/api/register", method=RequestMethod.POST)
	public Account register(@RequestParam String email, @RequestParam String name, HttpSession session){
		return accountService.register(email,name);
	}

	/**
	 * Starts a new register and create a new account if it has not been previously registered
	 */
	@RequestMapping(path="/api/registry/start", method=RequestMethod.POST)
	public Invitation startRegistry(@RequestParam String email, HttpSession session){
		return registryService.startNewRegistry(email);
	}

	/**
	 * Returns the give registry if the current user has an invitation
	 */
	@RequestMapping(path="/api/registry/{registryId}", method=RequestMethod.GET)
	public ResponseEntity<Registry> registry(@PathVariable int registryId, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");

		Registry registry = registryService.registry(registryId);
		if (registry == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		boolean invitationExists = invitationService.hasInvitation(currentUser.getId(), registry.getToken());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		return ResponseEntity.ok(registry);
	}

	/**
	 * Creates a new registry for the current user
	 */
	@RequestMapping(path="/api/registry/", method=RequestMethod.POST)
	public Invitation createRegsitry(@RequestParam String name, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");

		return registryService.createRegistry(currentUser.getId(), name);
	}

	/**
	 * Updates the registry, must be owner to update the registry
	 */
	@RequestMapping(path="/api/registry/{registryId}", method=RequestMethod.PUT)
	public ResponseEntity<Registry> updateRegsitry(@PathVariable int registryId, @RequestBody Registry registry, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");

		boolean isOwner = registryService.isOwner(currentUser.getId(), registryId);
		if (!isOwner) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		registry = registryService.updateRegistry(currentUser.getId(), registryId, registry);
		return ResponseEntity.ok(registry);
	}

	/**
	 * Creates an invitation to a registry. The current user must be the owner to
	 * create an invitation.
	 */
	@RequestMapping(path="/api/registry/{registryId}/invitation", method=RequestMethod.POST)
	public ResponseEntity<Invitation> createInvitation(@PathVariable int registryId, @RequestParam  String email, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");

		Registry registry = registryService.registry(registryId);
		boolean isOwner = registryService.isOwner(currentUser.getId(), registry.getId());
		if (!isOwner) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		Invitation invitation = invitationService.createInvitation(registry.getToken(), email);

		return ResponseEntity.ok(invitation);
	}

	/**
	 * Retrieves the registry associated with the invitation.
	 */
	@RequestMapping(path="/api/invitation/{token}", method=RequestMethod.GET)
	public ResponseEntity<Registry> inventation(@PathVariable String token, HttpSession session){

		Invitation invitation = invitationService.invitation(token);
		if (invitation == null)
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);

		accountService.authenicateUser(invitation.getInvitedUserId(), session);

		Registry registry = registryService.registryByToken(invitation.getTokenToAccess());

		return ResponseEntity.ok(registry);
	}

	/**
	 * Resends an existing invitation.
	 */
	@RequestMapping(path="/api/invitation/{token}/resend", method=RequestMethod.POST)
	public ResponseEntity<Void> resendInvitation(@PathVariable String token, @RequestParam String email, HttpSession session){

		registryService.sendInvitation(email, token);

		return ResponseEntity.ok().build();
	}


	/**
	 * Retrieves the items in the registry, must have an invitation
	 */
	@RequestMapping(path="/api/registry/{registryId}/item", method=RequestMethod.GET)
	public ResponseEntity<List<RegistryItem>> registryItems(@PathVariable int registryId, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");
		Registry registry = registryService.registry(registryId);


		boolean invitationExists = invitationService.hasInvitation(currentUser.getId(), registry.getToken());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		List<RegistryItem> wishlistItem = registryService.registryItems(registryId);

		//filter secret gifts from owner
		boolean isOwner = registryService.isOwner(currentUser.getId(), registryId);
		if (isOwner) {
			wishlistItem = wishlistItem.stream().filter(g -> !g.isSecret()).collect(Collectors.toList());
			wishlistItem.forEach(g -> g.setPurchased(false));
		}

		return ResponseEntity.ok(wishlistItem);
	}

	/**
	 * Adds an item to the registry
	 */
	@RequestMapping(path="/api/registry/{registryId}/item", method=RequestMethod.POST)
	public ResponseEntity<RegistryItem> addRegistryItem(@PathVariable int registryId, @RequestBody RegistryItem registryItem, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");
		Registry registry = registryService.registry(registryId);

		boolean invitationExists = invitationService.hasInvitation(currentUser.getId(), registry.getToken());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		registryItem = registryService.addRegistryItem(registryId, currentUser.getId(), registryItem);

		GiftAddEvent event = new GiftAddEvent();
		event.who = currentUser.getName();
		event.registryId = registryId;
		event.giftId = registryItem.getId();
		eventBus.post(event);

		return ResponseEntity.ok(registryItem);
	}

	/**
	 * Fetches metadata from a URL
	 */
	@RequestMapping(path="/api/url", method=RequestMethod.POST)
	public ResponseEntity<Map<String,String>> fetchMetadataInformation(@RequestParam String pageUrl, HttpSession session){

		URL url;
		try {
			url = new URL(pageUrl);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		Map<String, String> metadata = scraperService.downloadPage(url);
		return ResponseEntity.ok(metadata);
	}

	/**
	 * Update a registry item, must be the owner of the item
	 */
	@RequestMapping(path="/api/registry/{registryId}/item/{registryItemId}", method=RequestMethod.PUT)
	public ResponseEntity<RegistryItem> updateRegistryItem(@PathVariable int registryId, @PathVariable int registryItemId, @RequestBody RegistryItem registryItem, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");
		Registry registry = registryService.registry(registryId);

		boolean invitationExists = invitationService.hasInvitation(currentUser.getId(), registry.getToken());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		registryService.updateRegistryItem(currentUser.getId(), registryItemId, registryItem);

		return ResponseEntity.ok(registryItem);

	}

	/**
	 * Delete registry item
	 */
	@RequestMapping(path="/api/registry/{registryId}/item/{registryItemId}", method=RequestMethod.DELETE)
	public ResponseEntity<Void> deleteRegistryItem(@PathVariable int registryId, @PathVariable int registryItemId, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");
		Registry registry = registryService.registry(registryId);

		boolean invitationExists = invitationService.hasInvitation(currentUser.getId(), registry.getToken());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		registryService.deleteRegistryItem(currentUser.getId(), registryItemId);

		return ResponseEntity.ok().build();

	}

	/**
	 * Creates a registry group and creates registries for each email. Will also registry email
	 * if not previously registered
	 */
	@RequestMapping(path="/api/group/{token}", method=RequestMethod.POST)
	public ResponseEntity<String> createGroup(@PathVariable String token, @RequestParam String[] email, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");

		String groupToken = registryService.createGroup(token, currentUser.getId(), email);
		return ResponseEntity.ok(groupToken);
	}

	/**
	 * Adds a user to a registry group, no registry is created for this user.  Invitations are created
	 * for every registry the current user has an invitation for
	 */
	@RequestMapping(path="/api/group/{token}/invite", method=RequestMethod.PUT)
	public ResponseEntity<Void> inviteUserToGroup(@PathVariable String token, @RequestParam String email, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");

		//load registry for this group
		List<Registry> groupRegistry = registryService.groupRegistries(token);
		if (groupRegistry == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		//remove any registries the current user does not have an invitation for
		Iterator<Registry> iterator = groupRegistry.iterator();
		while (iterator.hasNext()) {
			Registry registry = iterator.next();
			boolean invitationExists = invitationService.hasInvitation(currentUser.getId(), registry.getToken());
			if (!invitationExists) {
				iterator.remove();
			}
		}

		if (groupRegistry.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		//create account and create invitations
		Account newAccount = accountService.register(email);
		for (Registry registry : groupRegistry){
			invitationService.createInvitation(registry.getToken(), newAccount.getId());
		}

		//send invitations to group
		registryService.sendGroupInvitation(email, token);

		return ResponseEntity.ok().build();
	}

	/**
	 * Sends an group invitation to the email address
	 */
	@RequestMapping(path="/api/group/{token}/invitation", method=RequestMethod.POST)
	public ResponseEntity<Void> sendGroupInvitation(@PathVariable String token, @RequestParam String email, HttpSession session){
		registryService.sendInvitation(email, token);
		return ResponseEntity.ok().build();
	}


	@RequestMapping(path="/api/mfa", method=RequestMethod.POST)
	public ResponseEntity<String> requestMFAAuthorization(@RequestParam String phone, HttpSession session){

		 try {
			String token = accountService.sendMFAMessage(phone);

			return ResponseEntity.ok(token);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
		}

	}

	@RequestMapping(path="/api/mfa", method=RequestMethod.GET)
	public ResponseEntity<Void> validateMFACode(@RequestParam String token, @RequestParam String code, HttpSession session){
		try {
			String authenticationToken = accountService.validatedMFAMessage(token, code);
			accountService.authenicateUser(authenticationToken, session);

			return ResponseEntity.status(HttpStatus.OK).build();

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	@RequestMapping(path="/api/group/{token}", method=RequestMethod.GET)
	public ResponseEntity<List<Registry>> group(@PathVariable String token, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");

		//load all registry for this group
		List<Registry> groupRegistry = registryService.groupRegistries(token);
		if (groupRegistry == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		//remove any registries the current user does not have an invitation for
		Iterator<Registry> iterator = groupRegistry.iterator();
		while (iterator.hasNext()) {
			Registry registry = iterator.next();
			boolean invitationExists = invitationService.hasInvitation(currentUser.getId(), registry.getToken());
			if (!invitationExists) {
				iterator.remove();
			}
		}

		if (groupRegistry.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		return ResponseEntity.ok(groupRegistry);
	}

	/**
	 * Returns the registry in the group owned by the current user
	 */
	@RequestMapping(path="/api/group/{token}/default", method=RequestMethod.GET)
	public ResponseEntity<Registry> groupUserRegistery(@PathVariable String token, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");

		List<Registry> groupRegistry = registryService.groupRegistries(token);
		if (groupRegistry == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		Iterator<Registry> iterator = groupRegistry.iterator();
		while (iterator.hasNext()) {
			Registry registry = iterator.next();
			boolean isOwner = registryService.isOwner(currentUser.getId(), registry.getId());
			if (isOwner) {
				return ResponseEntity.ok(registry);
			}
		}

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
	}

	/**
	 * Marks gift as purchased
	 */
	@RequestMapping(path="/api/registry/{registryId}/item/{registryItemId}/purchased", method=RequestMethod.PUT)
    public ResponseEntity<Void> purchasedToggle(@PathVariable int registryId, @PathVariable Integer registryItemId, HttpSession session){
		Account currentUser = (Account)session.getAttribute("user");
		Registry registry = registryService.registry(registryId);

		boolean invitationExists = invitationService.hasInvitation(currentUser.getId(), registry.getToken());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		registryService.purchasedRegistryItem(registryItemId, currentUser.getName());

		GiftPurchasedEvent event = new GiftPurchasedEvent();
		event.who = currentUser.getName();
		event.registryId = registryId;
		event.giftId = registryItemId;
		eventBus.post(event);

        return ResponseEntity.ok().build();
	}

}