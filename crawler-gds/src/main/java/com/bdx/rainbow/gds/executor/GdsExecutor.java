package com.bdx.rainbow.gds.executor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdx.rainbow.crawler.pool.Executor;
import com.bdx.rainbow.crawler.pool.ThreadPoolExecutorFactory.PoolType;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.crawler.task.SeedTask;
import com.bdx.rainbow.gds.entity.KeywordGds;
import com.bdx.rainbow.gds.entity.KeywordGdsExample;
import com.bdx.rainbow.gds.mapper.KeywordGdsMapper;
import com.bdx.rainbow.gds.seed.GdsPageSeed;

/**
 * @author mler
 */
public class GdsExecutor extends Executor {
	
	private final static Logger logger = LoggerFactory.getLogger(GdsExecutor.class);
	
	public static AtomicLong good_success = new AtomicLong(0);
	
	private boolean shutdown;
	
	public GdsExecutor(final String name) throws Exception {
		super(name);
	}
	
	public GdsExecutor(final String name,final String poolName,PoolType type) throws Exception {
		super(name,poolName,type);
	}

	/**
	 * 从数据库中获取一个关键字生成任务进行查询，如果未查到，则每个
	 */
	@Override
	public void startup() throws Exception {
		
		KeywordGdsMapper mapper = (KeywordGdsMapper) beanFactory.getBean(KeywordGdsMapper.class);
		
		/**
		 * 根据外部状态删除
		 */
		do{
			String key = getNextKey();
			if(StringUtils.isBlank(key))
			{
				Thread.sleep(1000*60*10);
				continue;
			}
			
			long t1 = System.currentTimeMillis();
			
			KeywordGds keyword = mapper.selectByPrimaryKey(key);
	   	    /** 根据keyword生成抓去任务 **/
			GdsPageSeed seed = new GdsPageSeed();
			seed.setKeyword(keyword.getKeyword());
			Collection<Seed> root_seeds = new ArrayList<Seed>();
			root_seeds.add(seed);
			SeedTask task = new SeedTask(root_seeds,keyword.getKeyword());
			addTask(task);
			
			/** 更新数据库中关键字的状态 **/
			keyword.setComplete("1");
			keyword.setStartTime(new Timestamp(System.currentTimeMillis()));
			mapper.updateByPrimaryKeySelective(keyword);
			
			restart();

			/** 更新数据库中关键字的状态 **/
			keyword.setComplete("2");
			keyword.setSuccess(good_success.get());
			keyword.setEndTime(new Timestamp(System.currentTimeMillis()));
			mapper.updateByPrimaryKeySelective(keyword);
			
			logger.info("================执行完毕 ["+name+"]["+key+"][执行成功个数："+good_success.get()+"][完成结果处理时间："+(System.currentTimeMillis()-t1)+" ms ][thread_submit:"+thread_submit.get()+" == thread_completed:"+thread_completed.get()+"]==================");
			
			
			/** 状态信息已经存储到Map中，一个key对应一个状态对象，因此不需要重置了 **/
			resetParameter();
			
		}while(shutdown == false);
	}
	
	private String getNextKey() throws Exception
	{
		KeywordGdsMapper mapper = (KeywordGdsMapper) beanFactory.getBean(KeywordGdsMapper.class);
		KeywordGdsExample where = new KeywordGdsExample();
   	    where.createCriteria().andCompleteEqualTo("0");
   	    where.setLimitClauseStart(0);
   	    where.setLimitClauseCount(1);
   	    
   	    List<KeywordGds> keywords = null;
   	    do{
   	    	keywords = mapper.selectByExample(where);
   	    	if(keywords == null || keywords.isEmpty()
   					|| StringUtils.isBlank(keywords.get(0).getKeyword()))
   	    	{
   	    		/** 如果未取到关键字，则线程等待30分钟再次获取 **/
   	   	    	logger.warn("can't find the keyword");
   	   	    	Thread.sleep(1000*60*10);
   	    	}
   	    	else
   	    		break;
   	    }
   	    while(true);
   	    
   	    /** 更具关键字生成爬虫任务 **/
   	    return keywords.get(0).getKeyword();
	}
	
	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	/** 虚拟机退出时的处理 **/
	@Override
	public void cleanBeforeShutdown() {
		
	}
	
}
