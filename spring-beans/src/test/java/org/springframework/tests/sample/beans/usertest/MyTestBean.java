package org.springframework.tests.sample.beans.usertest;

/**
 * @author rtw
 * @since 2019/11/27
 */
public abstract class MyTestBean {
	//用于测试lookup-method
	public abstract User getUserBean();

	private User user;
	//用于测试replace-method
	public void changedMethod(String str) {

		System.out.println("Origin method in MyTestBean run" + str);
	}

	public MyTestBean() {
	}
	//setter注入
	public void setUser(User user) {
		this.user = user;
	}

}
