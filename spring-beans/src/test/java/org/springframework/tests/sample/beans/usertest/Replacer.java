package org.springframework.tests.sample.beans.usertest;

import java.lang.reflect.Method;
import org.springframework.beans.factory.support.MethodReplacer;

/**
 * @author rtw
 * @since 2019/11/27
 */
public class Replacer implements MethodReplacer {

	/**
	 * Reimplement the given method.
	 *
	 * @param obj    the instance we're reimplementing the method for
	 * @param method the method to reimplement
	 * @param args   arguments to the method
	 * @return return value for the method
	 */
	@Override
	public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
		System.out.println("replacer method run");
		return null;
	}
}
