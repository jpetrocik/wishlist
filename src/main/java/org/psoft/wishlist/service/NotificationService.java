package org.psoft.wishlist.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.psoft.wishlist.dao.UserDao;
import org.psoft.wishlist.dao.data.WishlistUser;
import org.psoft.wishlist.service.events.GiftAddEvent;
import org.psoft.wishlist.service.events.GiftPurchasedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Component
public class NotificationService {
	private static Log log = LogFactory.getLog(NotificationService.class);
	
	@Value("${notification.period:14400000}")
	long period;
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	EmailerService emailer;
	
	@Autowired
	EventBus eventBus;
	
	@Autowired
	Timer timer;

	List<GiftAddEvent> addedGifts = new ArrayList<>();
	
	List<GiftPurchasedEvent> purchasedGifts = new ArrayList<>();

	@PostConstruct
	public void init() {
		eventBus.register(this);
		
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				log.info("Checking for notifications");
				sendEmail();
			}
			
		}, 30000, period);
	}

	protected void sendEmail() {
		for (WishlistUser user : userDao.findAll()) {
			List<GiftPurchasedEvent> purchasedEvents = purchasedGifts.stream().filter(f-> !f.initials.equals(user.getInitials())).collect(Collectors.toList());
			List<GiftAddEvent> addEvents = addedGifts.stream().filter(f-> !f.initials.equals(user.getInitials())).collect(Collectors.toList());
			if (addEvents.isEmpty() && purchasedEvents.isEmpty()) {
				continue;
			}
			
			StringBuilder body = new StringBuilder();
			if (!purchasedEvents.isEmpty()) {
				body.append("**************** Purchased Gifts ****************\n\n");
				for (GiftPurchasedEvent e : purchasedEvents ) {
					body.append(e.toString() + "\n\n");
				}
			}
			
			if (!addEvents.isEmpty()) {
				if (body.length() > 0)
					body.append("\n\n\n\n");
				body.append("**************** Gift Additions ****************\n\n");
				for (GiftAddEvent e : addEvents ) {
					body.append(e.toString() + "\n\n");
				}
			}

			try {
				log.info("Sending notification email to " + user.getEmail());
				emailer.sendMail(user.getEmail(), "no-reply@petrocik.net", "XMas List Updates", body.toString());
			} catch (Exception e1) {
				log.error("Unable to send email to " + user.getEmail(), e1);
			}
		}
		
		addedGifts.clear();
		purchasedGifts.clear();
		
	}

	@Subscribe
	public void eventGiftAdd(GiftAddEvent e) {
		log.info("Recieve giftAddEvent for " + e.initials);
		addedGifts.add(e);
	}

	@Subscribe
	public void eventGiftPurchased(GiftPurchasedEvent e) {
		log.info("Recieve giftPurchasedEvent for " + e.initials);
		purchasedGifts.add(e);
	}

}
