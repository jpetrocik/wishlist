package org.psoft.wishlist.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.psoft.wishlist.dao.data.Invitation;
import org.psoft.wishlist.dao.data.Registry;
import org.psoft.wishlist.dao.data.RegistryItem;
import org.psoft.wishlist.dao.data.WishlistUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WishListServiceApi {
	
	@Autowired
	RegistryService wishListService;
	
	@RequestMapping(path="/api/register", method=RequestMethod.POST)
	public WishlistUser register(@RequestParam String email, @RequestParam String name, HttpSession session){
		return wishListService.register(email,name);
	}

	/**
	 * Auto registers a new user and creates a registry
	 */
	@RequestMapping(path="/api/start", method=RequestMethod.POST)
	public Invitation startRegistry(@RequestParam String email, HttpSession session){
		return wishListService.startNewRegistry(email);
	}

	@RequestMapping(path="/api/registry", method=RequestMethod.POST)
	public Invitation createRegsitry(@RequestParam String name, HttpSession session){
		WishlistUser wishlistUser = (WishlistUser)session.getAttribute("user");

		return wishListService.createRegistry(wishlistUser.getId(), name);
	}

	@RequestMapping(path="/api/invitation", method=RequestMethod.POST)
	public ResponseEntity<Invitation> createInventation(@RequestParam int registryId, @RequestParam  String email, HttpSession session){
		WishlistUser wishlistUser = (WishlistUser)session.getAttribute("user");

		boolean isOwner = wishListService.isOwner(wishlistUser.getId(), registryId);
		if (!isOwner) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		Invitation invitation = wishListService.createInvitation(registryId, email);
		
		return ResponseEntity.ok(invitation);
	}

	@RequestMapping(path="/api/invitation/{token}", method=RequestMethod.GET)
	public ResponseEntity<Registry> inventation(@PathVariable String token, HttpSession session){

		Invitation invitation = wishListService.invitation(token);
		if (invitation == null)
			return ResponseEntity.status(403).body(null);
		
		WishlistUser wishlistUser = wishListService.wishListUser(invitation.getInvitedUserId());
		session.setAttribute("user", wishlistUser);
		
		Registry registry = wishListService.registry(invitation.getRegistryId());
		
		return ResponseEntity.ok(registry);
	}

	@RequestMapping(path="/api/user/registry/default", method=RequestMethod.GET)
	public ResponseEntity<Registry> defaultUserRegsiter(HttpSession session){
		WishlistUser wishlistUser = (WishlistUser)session.getAttribute("user");

		Registry registry = wishListService.findDefaultRegistery(wishlistUser.getId());
		return ResponseEntity.ok(registry);
	}

	/**
	 * Get registry
	 */
	@RequestMapping(path="/api/registry/{token}", method=RequestMethod.GET)
	public ResponseEntity<Registry> registry(@PathVariable String token, HttpSession session){
		WishlistUser wishlistUser = (WishlistUser)session.getAttribute("user");

		Registry registry = wishListService.registry(token);
		if (registry == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}

		boolean invitationExists = wishListService.hasInvitation(registry.getId(), wishlistUser.getId());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		return ResponseEntity.ok(registry);
	}

	/**
	 * Get registry items
	 */
	@RequestMapping(path="/api/registry/{registryId}/item", method=RequestMethod.GET)
	public ResponseEntity<List<RegistryItem>> registryItems(@PathVariable int registryId, HttpSession session){
		WishlistUser wishlistUser = (WishlistUser)session.getAttribute("user");
		
		boolean invitationExists = wishListService.hasInvitation(registryId, wishlistUser.getId());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		List<RegistryItem> wishlistItem = wishListService.registryItems(registryId);
		
		//filter secret gifts from owner
		boolean isOwner = wishListService.isOwner(registryId, wishlistUser.getId());
		if (isOwner) {
			List<RegistryItem> wishList = wishlistItem.stream().filter(g -> !g.isSecret()).collect(Collectors.toList());
			wishList.forEach(g -> g.setPurchased(false));
		}
		
		return ResponseEntity.ok(wishlistItem);
	}

	/**
	 * Add registry item
	 */
	@RequestMapping(path="/api/registry/{registryId}/item", method=RequestMethod.POST)
	public ResponseEntity<RegistryItem> addRegistryItem(@PathVariable int registryId, @RequestBody RegistryItem registryItem, HttpSession session){
		WishlistUser wishlistUser = (WishlistUser)session.getAttribute("user");

		boolean invitationExists = wishListService.hasInvitation(registryId, wishlistUser.getId());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		registryItem = wishListService.addRegistryItem(registryId, wishlistUser.getId(), registryItem);

		return ResponseEntity.ok(registryItem);
	}

	/**
	 * Update registry item
	 */
	@RequestMapping(path="/api/registry/{registryId}/item/{giftId}", method=RequestMethod.PUT)
	public ResponseEntity<RegistryItem> updateRegistryItem(@PathVariable int registryId, @PathVariable int giftId, @RequestBody RegistryItem registryItem, HttpSession session){
		WishlistUser wishlistUser = (WishlistUser)session.getAttribute("user");

		boolean invitationExists = wishListService.hasInvitation(registryId, wishlistUser.getId());
		if (!invitationExists) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		wishListService.updateRegistryItem(registryId, wishlistUser.getId(), registryItem);
		
//		GiftAddEvent event = new GiftAddEvent();
//		event.who = wishlistUser.getName();
//		event.gift = registryItem;
//		eventBus.post(event);

		return ResponseEntity.ok(registryItem);
		
	}

	/**
	 * Delete registry item
	 */
	@RequestMapping(path="/api/registry/{registryId}/item/{giftId}", method=RequestMethod.DELETE)
	public RegistryItem deleteRegistryItem(@PathVariable int registryId, @PathVariable int giftId, HttpSession session){
		throw new UnsupportedOperationException("Not yet implemented");
		
	}

//	/**
//	 * Mark gift as purchased
//	 */
//    @RequestMapping(path="/api/registry/{registryId}/item/{giftId}/purchased", method=RequestMethod.PUT)
//    public ResponseEntity<RegistryItem> purchasedToggle(@PathVariable int registryId, @PathVariable Integer giftId, HttpSession session){
//		WishlistUser wishlistUser = (WishlistUser)session.getAttribute("user");
//
//		boolean inventationDoesNotExists = isNull(wishListDao.invitation(registryId, wishlistUser.getId()));
//		if (inventationDoesNotExists) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
//		}
//
//		wishListDao.purchasedRegistryItem(giftId, wishlistUser.getName());
//
//		RegistryItem gift = wishListDao.registryItem(giftId);
//		
//		if (gift.isPurchased()) {
//			GiftPurchasedEvent event = new GiftPurchasedEvent();
//			event.who = wishlistUser.getName();
//			event.gift = gift;
//			eventBus.post(event);
//		}
//		
//        return ResponseEntity.ok(gift);
//	}
    
}