package org.springframework.jdbc;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.rtwtest.jdbc.User;
import org.springframework.jdbc.rtwtest.jdbc.UserService;

/**
 * @author rtw
 * @since 2019/12/27
 */
public class SpringJDBCTest {
	@Test
	public void test() {
		ApplicationContext act = new ClassPathXmlApplicationContext("org/springframework/jdbc/config/jdbc-config-rtw.xml");

		UserService userService = (UserService) act.getBean("userService");

		User user = new User();
		user.setAge(24);
		user.setName("小丁");
		user.setSex("男");
		userService.save(user);
		List<User> userList = userService.getUser();
		System.out.println(userList.toString());
	}

}
