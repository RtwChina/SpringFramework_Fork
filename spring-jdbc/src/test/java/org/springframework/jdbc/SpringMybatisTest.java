package org.springframework.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.rtwtest.mybatis.service.MyBatisService;

/**
 * @author rtw
 * @since 2019/12/29
 */
public class SpringMybatisTest {
	@Test
	public void test() {
		ApplicationContext act = new ClassPathXmlApplicationContext("org/springframework/jdbc/config/jdbc-config-rtwmybatis.xml");
		MyBatisService myBatisService = (MyBatisService)act.getBean("myBatisServiceImpl");
		myBatisService.insertAMybatisBean("丁丁", 22, "wu");
	}
}
