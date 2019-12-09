package org.springframework.tests.sample.beans.usertest.pdw;

import java.lang.reflect.Method;
import org.springframework.beans.factory.support.MethodReplacer;

/**
 * @author pengdengwang
 * @since 2019/12/7
 */
public class ChangeMethodReplacer implements MethodReplacer {
	@Override
	public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
		System.out.println("replace origin method!");
		return "替换原来的方法";
	}
}
