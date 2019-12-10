package org.springframework.tests.sample.beans.usertest.pdw;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 包含所有属性的测试Bean
 *
 * @author pengdengwang
 * @since 2019/12/7
 */
public class PdwTestBean {

	@Autowired
	@Qualifier("s")
	private People people;

	private String desc;

	private Map<String, Object> map;

	public PdwTestBean() {
	}

	public PdwTestBean(String desc, Map<String, Object> map) {
		this.desc = desc;
		this.map = map;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

	public People getPeople() {
		return people;
	}

	public void setPeople(People people) {
		this.people = people;
	}
}
