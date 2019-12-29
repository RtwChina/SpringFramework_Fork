package org.springframework.jdbc.rtwtest.mybatis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.rtwtest.mybatis.MyBatisUser;
import org.springframework.jdbc.rtwtest.mybatis.mapper.MyBatisUserMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author rtw
 * @since 2019/12/29
 */
@Component
public class MyBatisServiceImpl implements MyBatisService {

	@Autowired
	private MyBatisService2 myBatisService2;
	@Autowired
	private MyBatisUserMapper myBatisUserMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertAMybatisBean(String name, int age, String sex) {
		MyBatisUser myBatisUser = new MyBatisUser();
		myBatisUser.setAge(22);
		myBatisUser.setName("丁丁");
		myBatisUser.setSex("无");
		myBatisUserMapper.insertUser(myBatisUser);
		throw new RuntimeException();
	}

}
