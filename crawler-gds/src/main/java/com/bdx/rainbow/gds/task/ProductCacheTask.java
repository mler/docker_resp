package com.bdx.rainbow.gds.task;

import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdx.rainbow.crawler.bean.BeanFactory;
import com.bdx.rainbow.crawler.bean.BeanFactoryAware;
import com.bdx.rainbow.crawler.bean.SeedBeanDefinition;
import com.bdx.rainbow.crawler.pool.ThreadPoolExecutorFactory;
import com.bdx.rainbow.crawler.pool.ThreadPoolExecutorFactory.PoolType;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.crawler.task.SeedTask;
import com.bdx.rainbow.crawler.task.SeedTaskCache;
import com.bdx.rainbow.gds.seed.GdsProductSeed;

public class ProductCacheTask implements Runnable,BeanFactoryAware {

	private Logger logger = LoggerFactory.getLogger(ProductCacheTask.class);
	
	private transient BeanFactory beanFactory = BeanFactory.instance();
	
	@Override
	public void run() {
		
		logger.debug("========================定时任务开始("+System.currentTimeMillis()+")===========================");
		
		Collection<Seed> seeds = (Collection<Seed>)SeedTaskCache.getNext(GdsProductSeed.class,1);
		
		if(seeds == null || seeds.isEmpty())
			logger.warn("SeedTaskCache [GdsProductSeed] is empty");
		
		SeedTask task = new SeedTask(seeds,this.getClass().getName());
		
		SeedBeanDefinition seedBD = beanFactory.lookupSeedBeanDefinition(GdsProductSeed.class);
		String executorName = seedBD.getExecutor();
		ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor)ThreadPoolExecutorFactory.INSTANCE.getExecutorInstance(executorName,PoolType.SCHEDULE,10);
		executor.submit(task);
		
		logger.debug("========================定时任务结束("+System.currentTimeMillis()+")===========================");
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	
	
}
