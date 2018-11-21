package org.psoft.wishlist.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.psoft.wishlist.dao.data.Account;
import org.psoft.wishlist.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import com.plivo.api.PlivoClient;
import com.plivo.api.models.message.Message;

@Component
public class AccountDao {
	private static Log log = LogFactory.getLog(AccountDao.class);

	@Autowired
	DataSource dataSource;

	@Autowired
	PlivoClient plivoClient;

	JdbcTemplate jdbcTemplate;

	AccountRowMapper wishlistUserRowMapper = new AccountRowMapper();

	SimpleJdbcInsert jdbcWishListUserInsert;

	SimpleJdbcInsert jdbcSmsTokenInsert;

	public AccountDao() {
	}

	@PostConstruct
	public void init(){
		jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcWishListUserInsert = new SimpleJdbcInsert(jdbcTemplate);
		jdbcWishListUserInsert.withTableName("WISHLIST_USER").usingGeneratedKeyColumns("ID");

		jdbcSmsTokenInsert = new SimpleJdbcInsert(jdbcTemplate);
		jdbcSmsTokenInsert.withTableName("MFA_TOKEN");

	}

	public Account findByEmail(String email){
		try {
			return jdbcTemplate.queryForObject(
				    "select * from WISHLIST_USER where EMAIL=?",
				    new Object[] { email }, wishlistUserRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public Account findByPhone(String phone){
		try {
			return jdbcTemplate.queryForObject(
				    "select * from WISHLIST_USER where PHONE=?",
				    new Object[] { phone }, wishlistUserRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public Account findById(int id) {
		try {
			return jdbcTemplate.queryForObject(
				    "select * from WISHLIST_USER where ID=?",
				    new Object[] { id }, wishlistUserRowMapper);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public Collection<Account> findAll(){
		return jdbcTemplate.query(
			    "select * from WISHLIST_USER", wishlistUserRowMapper);
	}

	public Account register(String email, String name){
		String token = TokenGenerator.createToken(10);
		String authorizationToken = TokenGenerator.createToken(25);

		Map<String, Object> parameters = new HashMap<>();
        parameters.put("EMAIL", email);
        parameters.put("NAME", name);
        parameters.put("TOKEN", token);
        parameters.put("AUTHORIZATION_TOKEN", authorizationToken);

        Number key = jdbcWishListUserInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
        return new Account(key.intValue(), name, email);

	}

	public String generateMFAToken(String phone) throws Exception {
		Account account = findByPhone(phone);

		if (account == null)
			throw new AccountException();

		String code = String.format("%04d",
				(int)(Math.random()*10000));
		String token = TokenGenerator.createToken(10);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("ACCOUNT_ID", account.getId());
		parameters.put("TOKEN", token);
		parameters.put("CODE", code);

		jdbcSmsTokenInsert.execute(new MapSqlParameterSource(parameters));

		if (plivoClient.isTesting()) {
			System.out.println("Code: " + code);
		} else {
		Message.creator("15623178081", Collections.singletonList(phone), "Your verification code is " + code)
			.client(plivoClient)
			.create();
		}

		return token;
	}



	public Account validateAuthtoken(String authToken) {
		return jdbcTemplate.queryForObject(
			    "select * from WISHLIST_USER where AUTHORIZATION_TOKEN=?",
			    new Object[] { authToken }, wishlistUserRowMapper);
	}

	public String validateUser(String email, String token) {
		return jdbcTemplate.queryForObject(
			    "select AUTHORIZATION_TOKEN from WISHLIST_USER where EMAIL=? AND TOKEN=?",
			    new Object[] { email, token }, String.class);
	}

	public String validateMFAToken(String token, String code) {
		String accountId =  jdbcTemplate.queryForObject(
			    "select ACCOUNT_ID from MFA_TOKEN where CODE=? AND TOKEN=?",
			    new Object[] { code, token }, String.class);

		return generateUserAuthToken(Integer.parseInt(accountId));

	}

	public String generateUserAuthToken(int id) {
		return jdbcTemplate.queryForObject(
			    "select AUTHORIZATION_TOKEN from WISHLIST_USER where ID=?",
			    new Object[] { id }, new RowMapper<String>() {

					@Override
					public String mapRow(ResultSet rs, int rowNum) throws SQLException {
						return rs.getString("AUTHORIZATION_TOKEN");
					}

			    });
	}


	public class AccountRowMapper implements RowMapper<Account> {

		@Override
	    public Account mapRow(ResultSet results, int rowNum) throws SQLException {
			Account wishlistUser = new Account(
					results.getInt("ID"),
					results.getString("NAME"),
					results.getString("EMAIL"));

			return wishlistUser;
		}
	}

	public class AccountException extends Exception {

		public AccountException() {
			super();
		}
	}

}
