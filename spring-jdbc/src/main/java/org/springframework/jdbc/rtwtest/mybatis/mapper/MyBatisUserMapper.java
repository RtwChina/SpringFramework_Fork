package org.springframework.jdbc.rtwtest.mybatis.mapper;

import org.springframework.jdbc.rtwtest.mybatis.MyBatisUser;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * @author rtw
 * @since 2019/12/28
 */
@Repository
public interface MyBatisUserMapper {
	public void insertUser(MyBatisUser myBatisUser);

	public MyBatisUser getUser(Integer id);
}
