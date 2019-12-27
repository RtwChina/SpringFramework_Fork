package org.springframework.jdbc.rtwtest.jdbc;

import java.util.List;

/**
 * @author rtw
 * @since 2019/12/27
 */
public interface UserService {
	void save(User user);

	List<User> getUser();
}
