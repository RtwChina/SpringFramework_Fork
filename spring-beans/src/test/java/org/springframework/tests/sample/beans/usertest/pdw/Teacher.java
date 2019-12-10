package org.springframework.tests.sample.beans.usertest.pdw;

/**
 * @author pengdengwang
 * @since 2019/12/7
 */
public class Teacher extends People {
	@Override
	public void showMe() {
		System.out.println("I am teacher!");
	}
}
