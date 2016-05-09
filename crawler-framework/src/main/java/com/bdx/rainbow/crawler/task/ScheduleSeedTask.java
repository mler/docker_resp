package com.bdx.rainbow.crawler.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdx.rainbow.crawler.pool.ExecutorFactory;
import com.bdx.rainbow.crawler.seed.Seed;

/**
 * 将需要定时执行的任务放到队列中，以固定的间隔时间运行seed
 * @author mler
 * @2016年4月28日
 */
public class ScheduleSeedTask implements Runnable,Serializable {

	private static Logger logger = LoggerFactory.getLogger(ScheduleSeedTask.class);
	
	private String queueKey;
	
	private int fetchSize;
	
	private String groupId;
	
	public ScheduleSeedTask(String queueKey, int fetchSize, String groupId) {
		super();
		this.queueKey = queueKey;
		this.fetchSize = fetchSize;
		this.groupId = groupId;
	}

	public String getQueueKey() {
		return queueKey;
	}

	public void setQueueKey(String queueKey) {
		this.queueKey = queueKey;
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	@Override
	public void run() {
		
		logger.debug("time:"+System.currentTimeMillis()+".....................");
		
		if(StringUtils.isBlank(queueKey))
		{
			logger.error("seed queueKey is blank");
			return;
		}
		if(fetchSize <= 0)
			fetchSize = 1;
		List<Seed> seeds = ExecutorFactory.fetchQueueSeed(queueKey, fetchSize);
		
		try {
			Map<String,Collection<Seed>> error_seeds = new SeedProcessor(seeds, groupId).process();
			/**
			 * 讲出错的定时任务seed重新放回queue里面，等待下一次执行
			 */
			if(error_seeds != null && error_seeds.get(this.getGroupId()).isEmpty() == false)
			{
				for(Seed seed:error_seeds.get(this.getGroupId()))
					ExecutorFactory.addSeedToQueue(queueKey, seed);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
}
