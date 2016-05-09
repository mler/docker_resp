package com.bdx.rainbow.crawler.pool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

/**
 * 线程池工厂,枚举实现单例
 * @author mler
 * @2016年4月9日
 */
public enum ThreadPoolExecutorFactory {
	
	INSTANCE;
	
	public enum PoolType {
		COMMON,SCHEDULE;
	}
	
	public Map<String,ThreadPoolExecutor > poolMap;
	
	public ThreadPoolExecutor executor;
	
	public ScheduledThreadPoolExecutor scheduledExecutor;
	
	public final static String DEFAULT_POOL_NAME = "default_executor"; 
	
	public final static String DEFAULT_SCHEDULE_NAME = "default_executor"; 
	
	ThreadPoolExecutorFactory()
	{
		poolMap = new ConcurrentHashMap<String, ThreadPoolExecutor>(0);
		executor = new ThreadPoolExecutor(70,70, 3, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>()) ;
		scheduledExecutor = new ScheduledThreadPoolExecutor(10);
		register(DEFAULT_POOL_NAME,executor);
		register(DEFAULT_SCHEDULE_NAME,scheduledExecutor);
	}
	
	public ThreadPoolExecutor getExecutorInstance(String name,PoolType type,int coreSize)
	{
		try{
			if(name == null || StringUtils.isEmpty(name))
				return executor;
			
			if(name != null && poolMap.containsKey(name) == false)
			{
				ThreadPoolExecutor pool = null;
				
				if(PoolType.SCHEDULE.equals(type))
				{
					pool = new ScheduledThreadPoolExecutor(coreSize);
				}
				else
					pool = new ThreadPoolExecutor(coreSize,5, 3, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
			
				register(name,pool);
			}
			
			return poolMap.get(name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void register(String name,ThreadPoolExecutor pool)
	{
		poolMap.put(name, pool);
	}
}
