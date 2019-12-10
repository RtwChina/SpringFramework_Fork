package org.springframework.beans.factory.xml;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.tests.sample.beans.usertest.pdw.ChangeMethod;
import org.springframework.tests.sample.beans.usertest.pdw.LookUpMethodTestBean;
import org.springframework.tests.sample.beans.usertest.pdw.PdwTestBean;
import org.springframework.tests.sample.beans.usertest.pdw.People;

/**
 * @author pengdengwang
 * @since 2019/12/7
 */
public class PdwTest {
	private static final Class<?> CLASS = PdwTest.class;
	private static final String CLASSNAME = CLASS.getSimpleName();
	//	----------------user-xml-------------
	private static final ClassPathResource PDW_TEST = classPathResource("-pdwTest.xml");

	//	-------------END------------
	private static ClassPathResource classPathResource(String suffix) {
		return new ClassPathResource(CLASSNAME + suffix, CLASS);
	}

	/**
	 * 测试失败
	 */
	@Test
	public void testQualifier() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.loadBeanDefinitions(PDW_TEST);
		PdwTestBean bean = (PdwTestBean) xbf.getBean("pdwTestBean");
//		People bean = (People) xbf.getBean("people");
		bean.getPeople().showMe();

	}

	@Test
	public void testReplaceMethod()	{
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.loadBeanDefinitions(PDW_TEST);
		ChangeMethod changeMethod = (ChangeMethod) xbf.getBean("changeMethod");
		String changeMe = changeMethod.changeMe();
		System.out.println(changeMe);
	}

	@Test
	public void testLookUpMethod() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.loadBeanDefinitions(PDW_TEST);
		LookUpMethodTestBean lookUpMethodTestBean = (LookUpMethodTestBean) xbf.getBean("lookUpMethodTestBean");
		lookUpMethodTestBean.showMe();
	}
}
