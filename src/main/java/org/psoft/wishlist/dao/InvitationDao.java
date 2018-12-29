package org.psoft.wishlist.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.psoft.wishlist.dao.data.Invitation;
import org.psoft.wishlist.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

@Component
public class InvitationDao {

	@Autowired
	DataSource dataSource;

	SimpleJdbcInsert jdbcInvitationInsert;

	InvitationRowMapper invitationRowMapper = new InvitationRowMapper();

	JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void init() {
		jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcInvitationInsert = new SimpleJdbcInsert(jdbcTemplate);
		jdbcInvitationInsert.withTableName("REGISTRY_INVITATION").usingGeneratedKeyColumns("ID");
	}

	public Invitation invitation(String token) {
		try {
			Invitation invitation = jdbcTemplate.queryForObject("select * from REGISTRY_INVITATION where TOKEN=?",
					new Object[] { token }, invitationRowMapper);
			return invitation;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public Invitation createInvitation(String tokenToAccess, int invitedUserId) {
        String token = TokenGenerator.createToken(25);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("TOKEN_TO_ACCESS", tokenToAccess);
        parameters.put("INVITED_USER_ID", invitedUserId);
		parameters.put("TOKEN", token);

        Number key = jdbcInvitationInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
        return new Invitation(key.intValue(), tokenToAccess, invitedUserId, token);
	}

	public Invitation invitation(String tokenToAccess, int invitedUserId) {
		try {
			Invitation invitation = jdbcTemplate.queryForObject("select * from REGISTRY_INVITATION where TOKEN_TO_ACCESS=? AND INVITED_USER_ID=?",
					new Object[] { tokenToAccess, invitedUserId}, invitationRowMapper);
			return invitation;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}


	public class InvitationRowMapper implements RowMapper<Invitation> {

		@Override
	    public Invitation mapRow(ResultSet rs, int rowNum) throws SQLException {
			Invitation invitation = new Invitation(rs.getInt("ID"), rs.getString("TOKEN_TO_ACCESS"), rs.getInt("INVITED_USER_ID"),
					rs.getString("TOKEN"));
	        return invitation;
	    }
	}



}
