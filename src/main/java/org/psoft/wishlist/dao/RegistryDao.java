package org.psoft.wishlist.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.psoft.wishlist.dao.data.Registry;
import org.psoft.wishlist.dao.data.RegistryItem;
import org.psoft.wishlist.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

@Component
public class RegistryDao {
	private static Log log = LogFactory.getLog(RegistryDao.class);

	@Autowired
	DataSource dataSource;

	JdbcTemplate jdbcTemplate;

	SimpleJdbcInsert jdbcRegistryInsert;

	SimpleJdbcInsert jdbcRegistryItemsInsert;

	SimpleJdbcInsert jdbcRegistryGroupInsert;

	RegistryItemRowMapper registryItemRowMapper = new RegistryItemRowMapper();

	RegistryRowMapper registryRowMapper = new RegistryRowMapper();

	public RegistryDao() {
	}

	@PostConstruct
	public void  init() {
		jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcRegistryInsert = new SimpleJdbcInsert(jdbcTemplate);
		jdbcRegistryInsert.withTableName("REGISTRY").usingGeneratedKeyColumns("ID");

		jdbcRegistryItemsInsert = new SimpleJdbcInsert(jdbcTemplate);
		jdbcRegistryItemsInsert.withTableName("REGISTRY_ITEMS").usingGeneratedKeyColumns("ID");

		jdbcRegistryGroupInsert = new SimpleJdbcInsert(jdbcTemplate);
		jdbcRegistryGroupInsert.withTableName("REGISTRY_GROUP");
	}

	public Registry createRegistry(String name, int ownerId) {
        String token = TokenGenerator.createToken(10);

		Map<String, Object> parameters = new HashMap<>();
        parameters.put("NAME", name);
        parameters.put("OWNER_ID", ownerId);
        parameters.put("TOKEN", token);

        Number key = jdbcRegistryInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
		return registry(key.intValue());
	}

	public Registry registry(int registryId) {
		Registry registry = jdbcTemplate.queryForObject("select * from REGISTRY where ID=?",
				new Object[] { registryId}, registryRowMapper);
		return registry;
	}

	public Registry registryByToken(String token) {
		try {
			Registry registry = jdbcTemplate.queryForObject("select * from REGISTRY where TOKEN=?",
					new Object[] { token}, registryRowMapper);
			return registry;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

//	public Registry defaultRegistry(int userId) {
//		Registry registry  = jdbcTemplate.queryForObject("select * from REGISTRY where OWNER_ID=? AND IS_DEFAULT=1 ORDER BY ID asc",
//				new Object[] { userId }, registryRowMapper);
//		return registry;
//	}

	public List<RegistryItem> registryItems(int registryId) {
		List<RegistryItem> wishList  = jdbcTemplate.query("select * from REGISTRY_ITEMS where ACTIVE=1 AND REGISTRY_ID=? ORDER BY ID asc",
				new Object[] { registryId }, registryItemRowMapper);
		return wishList;
	}

	public RegistryItem registryItem(long giftId) {
		RegistryItem registryItem  = jdbcTemplate.queryForObject("select * from REGISTRY_ITEMS where ID=?",
				new Object[] { giftId }, registryItemRowMapper);
		return registryItem;
	}

	public RegistryItem createRegsitryItem(int registryId, int userId, RegistryItem gift) {
		Map<String, Object> parameters = new HashMap<>();
        parameters.put("REGISTRY_ID", registryId);
        parameters.put("OWNER_ID", userId);
        parameters.put("DESCR", gift.getDescr());
        parameters.put("IS_SECRET", gift.isSecret());
        parameters.put("IS_PURCHASED", false);
        parameters.put("ACTIVE", true);
        parameters.put("URL", gift.getUrl());

        Number key = jdbcRegistryItemsInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));

		return registryItem(key.intValue());
	}

	public RegistryItem updateRegistryItem(int ownerId,int registryItemId, RegistryItem gift) {
		jdbcTemplate.update("update REGISTRY_ITEMS set DESCR=?, URL=? where ID=? and OWNER_ID=?",
				gift.getDescr(), gift.getUrl(), registryItemId, ownerId);

		return registryItem(registryItemId);
	}

	public void deleteRegistryItem(int ownerId, int registryItemId) {
		jdbcTemplate.update("update REGISTRY_ITEMS set ACTIVE=0 where ID=? and OWNER_ID=?",
				registryItemId, ownerId);
	}

	public Registry updateRegistry(int ownerId, int registryId, Registry registry) {
		jdbcTemplate.update("update REGISTRY set NAME=? where ID=? and OWNER_ID=?",
				registry.getName(), registryId, ownerId);

		return registry(registryId);
	}

	public void purchasedRegistryItem(int giftId, String purchasedByUserId) {
		jdbcTemplate.update("update REGISTRY_ITEMS set IS_PURCHASED = !IS_PURCHASED, PURCHASED_BY=? where ID=?",
				purchasedByUserId, giftId);
	}

	public void createGroup(String token, Integer registryId) {
        Map<String, Object> parameters = new HashMap<>();
		parameters.put("TOKEN", token);
        parameters.put("REGISTRY_ID", registryId);

        jdbcRegistryGroupInsert.execute(new MapSqlParameterSource(parameters));
	}

	public List<Integer> group(String token) {
		List<Integer> registryIds  = jdbcTemplate.queryForList(
				"select REGISTRY_ID from REGISTRY_GROUP where TOKEN=? ORDER BY REGISTRY_ID",
				new Object[] { token }, Integer.class);
		return registryIds;
	}


	public class RegistryRowMapper implements RowMapper<Registry> {

		@Override
	    public Registry mapRow(ResultSet results, int rowNum) throws SQLException {
			Registry registry = new Registry(results.getInt("ID"), results.getInt("OWNER_ID"), results.getString("TOKEN"));
			registry.setName(results.getString("NAME"));
			return registry;
		}
	}

	public class RegistryItemRowMapper implements RowMapper<RegistryItem> {

		@Override
	    public RegistryItem mapRow(ResultSet results, int rowNum) throws SQLException {
			RegistryItem registryItem = new RegistryItem(results.getInt("ID"), results.getInt("OWNER_ID"), results.getInt("REGISTRY_ID"));
			registryItem.setDescr(results.getString("DESCR"));
			registryItem.setUrl(results.getString("URL"));
			registryItem.setSecret(results.getBoolean("IS_SECRET"));
			registryItem.setPurchased(results.getBoolean("IS_PURCHASED"));
			registryItem.setPurchasedBy(results.getString("PURCHASED_BY"));
			return registryItem;
		}
	}

}
