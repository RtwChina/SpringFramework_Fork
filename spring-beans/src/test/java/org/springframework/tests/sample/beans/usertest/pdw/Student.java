package org.springframework.tests.sample.beans.usertest.pdw;

/**
 * @author pengdengwang
 * @since 2019/12/7
 */
public class Student extends People {
	@Override
	public void showMe() {
		System.out.println("I am user!");
	}
}
