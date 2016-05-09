package com.bdx.rainbow.gds.configuration;

import java.io.Serializable;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlerConfig implements Serializable {

	public static boolean ifProxy = true;

	public boolean isIfProxy() {
		return ifProxy;
	}

	public void setIfProxy(boolean ifProxy) {
		this.ifProxy = ifProxy;
	}
	
}
