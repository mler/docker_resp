package com.bdx.rainbow.gds.service;

public class Hello implements IHello {

	@Override
	public String sayHello(String name) {
		return "hello,"+name;
	}

}
