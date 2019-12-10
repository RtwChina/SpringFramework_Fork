package org.springframework.autotest;

import org.springframework.stereotype.Service;

/**
 * @author rtw
 * @since 2019/12/6
 */
@Service
public class Hello {
	public void say() {
		System.out.println("hello");
	}
}
