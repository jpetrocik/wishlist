package org.psoft.wishlist.dao;


import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.psoft.wishlist.dao.data.Gift;

public class TestWishListDao {

	WishListDao uut;
	
	int giftId;

	@Before
	public void setup(){
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://hermes.petrocik.net:3306/personal");
		dataSource.setUsername("");
		dataSource.setPassword("");
		
		uut = new WishListDao();
		uut.dataSource = dataSource;
	}

	@Test
	public void testAddAndPurchase() {
		Gift gift = new Gift();
		gift.setDescr("Test Description");
		gift.setInitials("JMP");
		gift.setSecret(false);
		gift.setPurchased(false);
		gift.setPurchasedBy("EXP");
		gift.setTitle("Test Title");
		gift.setUrl("http://www.somewhere.com");
		
		gift = uut.save(gift);
		Assert.assertTrue(gift.getGiftId()>0);

		gift.setPurchased(true);
		gift.setPurchasedBy("EXP");
		
		uut.save(gift);
	}
	
}
