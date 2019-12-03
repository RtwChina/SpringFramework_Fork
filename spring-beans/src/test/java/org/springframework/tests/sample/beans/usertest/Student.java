package org.springframework.tests.sample.beans.usertest;

/**
 * @author rtw
 * @since 2019/11/27
 */
public class Student implements User{
	@Override
	public void showMe(String str) {
		System.out.println("I am a Student" + str);
	}
}
