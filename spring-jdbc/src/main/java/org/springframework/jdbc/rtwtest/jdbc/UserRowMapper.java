package org.springframework.jdbc.rtwtest.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author rtw
 * @since 2019/12/27
 */
public class UserRowMapper implements RowMapper<User> {
	@Override
	public User mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new User(rs.getInt("id"), rs.getString("name"),
				rs.getInt("age"), rs.getString("sex"));
	}
}
