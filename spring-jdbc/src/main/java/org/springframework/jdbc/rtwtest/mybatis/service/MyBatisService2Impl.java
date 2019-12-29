package org.springframework.jdbc.rtwtest.mybatis.service;

import org.springframework.stereotype.Service;

/**
 * @author rtw
 * @since 2019/12/29
 */
@Service
public class MyBatisService2Impl implements MyBatisService2 {
	@Override
	public void say() {
		System.out.println("你好");
	}
}
