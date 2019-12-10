package org.springframework.autotest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * @author rtw
 * @since 2019/12/6
 */
public class RtwTest implements BeanFactoryAware {
	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void testAware() {
		// 通过hello这个bean id从beanFactory获取实例
		Hello hello = (Hello) beanFactory.getBean("hello");
		hello.say();
	}
}
