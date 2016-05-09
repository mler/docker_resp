package com.bdx.rainbow.crawler.event;

import java.util.EventObject;

import com.bdx.rainbow.crawler.pool.Executor;

/** excutor的监听事件 **/
public class ExecutorEvent extends EventObject {

	public ExecutorEvent(Executor source) {
		super(source);
	}

	public Executor getExecutor()
	{
		return (Executor)super.getSource();
	}
	
}
