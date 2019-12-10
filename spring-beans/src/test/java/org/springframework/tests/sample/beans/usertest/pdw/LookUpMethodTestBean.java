package org.springframework.tests.sample.beans.usertest.pdw;

/**
 * 测试lookUp-method
 *
 * @author pengdengwang
 * @since 2019/12/7
 */
public abstract class LookUpMethodTestBean {

	public void showMe() {
		this.getBean().showMe();

	}

	public abstract People getBean();
}
