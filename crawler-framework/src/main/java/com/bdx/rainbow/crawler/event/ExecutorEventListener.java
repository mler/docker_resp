package com.bdx.rainbow.crawler.event;

import java.util.EventListener;

/**
 * Exceutor监听事件
 * @author mler
 * @2016年4月16日
 */
public interface ExecutorEventListener extends EventListener {

	public void beforeStartup(ExecutorEvent event) throws Exception;
	
	public void afterCompleted(ExecutorEvent event) throws Exception;
	
}
