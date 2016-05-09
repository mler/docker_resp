package com.bdx.rainbow.crawler.bean;

import java.io.Serializable;

import org.springframework.stereotype.Component;

@Component
public interface Initable extends Serializable {

	public void init();
	
}
