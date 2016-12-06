package org.psoft.wishlist.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.psoft.wishlist.dao.data.Gift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WishListDao {
	private static Log log = LogFactory.getLog(WishListDao.class);

	@Autowired
	DataSource dataSource;

	public int purchased(int giftId, String purchasedBy) {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			PreparedStatement statement = connection.prepareStatement("update GIFT set IS_PURCHASED = !IS_PURCHASED, "
					+ "PURCHASED_BY=? where GIFT_ID=? AND INITIALS<>?");
			statement.setString(1, purchasedBy);
			statement.setInt(2, giftId);
			statement.setString(3, purchasedBy);
			return statement.executeUpdate();
		} catch (Exception e) {
			String msg = "Unable to mark gift " + giftId + " as purchased";
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		} finally {
			try {
				connection.close();
			} catch (Exception e) {
				log.error("Failed to close connection", e);
			}
		}
	}
	

	public List<Gift> fetch(String name) {
		List<Gift> wishList = new ArrayList<Gift>();
		
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			PreparedStatement statement = connection.prepareStatement("select * from GIFT where INITIALS=? ORDER BY GIFT_ID asc");
			statement.setString(1, name);
			ResultSet results = statement.executeQuery();
			while(results.next()){
				wishList.add(toGift(results));
			}
		} catch (Exception e) {
			String msg = "Unable to fetch wish list for " + name;
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				log.error("Failed to close connection", e);
			}
		}

		return wishList;
	}

	public void save(List<Gift> wishList) {
		for (Gift gift : wishList){
			save(gift);
		}
	}
	
	public Gift save(Gift gift) {
		if (gift.getGiftId()>0)
			return update(gift);
		else
			return insert(gift);
	}
	
	private Gift insert(Gift gift) {
		Connection connection = null;
		
		if (StringUtils.isBlank(gift.getTitle()) ||
				StringUtils.isBlank(gift.getInitials())){
			throw new RuntimeException("Incomplete gift data");
		}

		try {
			connection = dataSource.getConnection();
			PreparedStatement statement = connection.prepareStatement("insert into GIFT (GIFT, DESCR,"
					+ "INITIALS, IS_SECRET, URL) values (?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, gift.getTitle());
			statement.setString(2, gift.getDescr());
			statement.setString(3, gift.getInitials());
			statement.setBoolean(4, gift.isSecret());
			statement.setString(5, gift.getUrl());
			statement.executeUpdate();
			
			ResultSet tableKeys = statement.getGeneratedKeys();
			tableKeys.next();
			gift.setGiftId(tableKeys.getInt(1));
			
			return gift;
			
		} catch (Exception e) {
			String msg = "Unable to save gift " + gift.getTitle();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		} finally {
			try {
				connection.close();
			} catch (Exception e) {
				log.error("Failed to close connection", e);
			}
		}
	}

	private Gift update(Gift gift) {
		Connection connection = null;
		
		if (StringUtils.isBlank(gift.getTitle()) ||
				StringUtils.isBlank(gift.getInitials()) ||
				gift.getGiftId() <1){
			throw new RuntimeException("Incomplete gift data");
		}
		
		try {
			connection = dataSource.getConnection();
			PreparedStatement statement = connection.prepareStatement("update GIFT set GIFT=?, DESCR=?, "
					+ "URL=? where GIFT_ID=?");
			statement.setString(1, gift.getTitle());
			statement.setString(2, gift.getDescr());
			statement.setString(3, gift.getUrl());
			statement.setInt(4, gift.getGiftId());
			statement.executeUpdate();
			
			return gift;
		} catch (Exception e) {
			String msg = "Unable to save gift " + gift.getTitle();
			log.error(msg, e);
			throw new RuntimeException(msg, e);
		} finally {
			try {
				connection.close();
			} catch (Exception e) {
				log.error("Failed to close connection", e);
			}
		}
	}

	private Gift toGift(ResultSet results) throws SQLException {
		Gift gift = new Gift();
		gift.setGiftId(results.getInt("GIFT_ID"));
		gift.setTitle(results.getString("GIFT"));
		gift.setDescr(results.getString("DESCR"));
		gift.setInitials(results.getString("INITIALS"));
		gift.setSecret(results.getBoolean("IS_SECRET"));
		gift.setPurchased(results.getBoolean("IS_PURCHASED"));
		gift.setPurchasedBy(results.getString("PURCHASED_BY"));
		gift.setUrl(results.getString("URL"));
		return gift;
	}

}
