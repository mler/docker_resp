package com.bdx.rainbow.crawler.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdx.rainbow.crawler.monitor.StatInfo;
import com.bdx.rainbow.crawler.seed.Seed;

/**
 * 自定义线程执行者工厂,通过枚举实现单例
 * @author mler
 * @2016年4月16日
 */
public enum ExecutorFactory {
	
	INSTANCE;
	
	private static Logger logger = LoggerFactory.getLogger(ExecutorFactory.class);
	
	public Map<String,Executor> executorMap;
	
	public Map<String,Map<Class<?>,Collection<Seed>>> errorSeedMap;
	
	public Executor defaultExecutor;
	
	public final static String DEFAULT_POOL_EXECUTOR = "default_executor";
	
	public final static String DEFAULT_SCHEDULE_EXECUTOR = "default_schedule";
	
	private static Map<String,Queue<Seed>> seedQueueMap = new ConcurrentHashMap<String, Queue<Seed>>();
	
	ExecutorFactory()
	{
		executorMap = new ConcurrentHashMap<String, Executor>(0);
	}
	
	public Executor getExecutor(String name)
	{
		name = StringUtils.isBlank(name)?DEFAULT_POOL_EXECUTOR : name; 
		try{
			if(StringUtils.isBlank(name))
				throw new Exception("初始化线程池出错，name is blank");
			
			return executorMap.get(name);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 注册executor
	 * @param executor
	 */
	public void registerExecutor(Executor executor)
	{
		executorMap.put(executor.getName(), executor);
	}
	
	
	public static synchronized void addSeedToQueue(String queueKey,Seed seed)
	{
		if(seed == null || StringUtils.isBlank(queueKey))
			logger.error("queueKey or seed is null");
		
		if(seedQueueMap.get(queueKey) == null)
			seedQueueMap.put(queueKey, new ConcurrentLinkedQueue<Seed>());
		
		seedQueueMap.get(queueKey).add(seed);
	}
	
	public static List<Seed> fetchQueueSeed(String queueKey,int num)
	{
		List<Seed> seeds = new ArrayList<Seed>(num);
		for(int i=0;i<num;i++)
		{
			Seed seed = seedQueueMap.get(queueKey).poll();
			if(seed != null)
				seeds.add(seed);
		}
		
		return seeds;
	}
	
	public void destory()
	{
		if(executorMap == null || executorMap.values().isEmpty())
			return;
		
		for(Executor executor : executorMap.values())
			executor.shutDown();
				
	}
	
}
