package org.springframework.tests.sample.beans.usertest;

/**
 * @author rtw
 * @since 2019/12/9
 */
public class MyTestBeanTwo {
	private MyTestBean myTestBean;

	private String name = "Two123";

	public void setName(String name) {
		this.name = name;
	}

	public void setMyTestBean(MyTestBean myTestBean) {
		this.myTestBean = myTestBean;
	}
}
