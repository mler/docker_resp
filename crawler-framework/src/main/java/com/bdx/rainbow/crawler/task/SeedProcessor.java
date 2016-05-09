package com.bdx.rainbow.crawler.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdx.rainbow.crawler.analyzer.Analyzer;
import com.bdx.rainbow.crawler.bean.BeanFactory;
import com.bdx.rainbow.crawler.bean.SeedBeanDefinition;
import com.bdx.rainbow.crawler.pool.ExecutorFactory;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.crawler.utils.JacksonUtils;

public class SeedProcessor {
	
	private static final long serialVersionUID = -513470183743154221L;

	public Logger logger = LoggerFactory.getLogger(SeedProcessor.class);
	
	protected transient long 							startTime;//开始时间
	
	protected transient long 							endTime;//结束时间
	
	protected String 						name;//线程名称
	
	protected Collection<Seed> 				seeds;//执行的种子
	
	/** bean工厂 **/
	private transient BeanFactory beanFactory = BeanFactory.instance();
	
	/** 自定义线程池工厂 **/
	private transient ExecutorFactory executorFactory = ExecutorFactory.INSTANCE;
	
	/**
	 * 任务的groupid,当根任务完成后，此根任务生成的所有子任务的groupid相同
	 */
	public final String groupId;
	
	public SeedProcessor(Collection<Seed> seeds,final String groupId) {
		super();
		this.seeds = seeds;
		this.groupId = groupId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 将根据返回的seed生成新的task
	 * @param seed
	 * @return
	 * @throws Exception
	 */
	private void processNewSeed(Map<Class<? extends Seed>,Collection<Seed>> seedsMap) throws Exception{
		if(seedsMap != null && seedsMap.isEmpty() == false)
		{
			for(Class<?> seedClass : seedsMap.keySet())
			{
				if(seedsMap.get(seedClass) == null || seedsMap.get(seedClass).isEmpty())
					continue;
				
				SeedBeanDefinition seedBD = beanFactory.lookupSeedBeanDefinition(seedClass);
				
				/**
				 * 根据注释schedule属性来判断这个种子是否是定时执行的还是即时执行
				 */
				if(seedBD.isSchedule() == false)
				{
					submitToExecutor(seedBD,seedsMap.get(seedClass));
				}
				else
				{
					submitToScheduleExecutor(seedBD, seedsMap.get(seedClass));
				}
			}
		}
		
	}
	
	public Map<String,Collection<Seed>> process() throws Exception {
		
		if (seeds == null && seeds.isEmpty())
			return null;
		
		Map<String,Collection<Seed>> error_seeds = new HashMap<String,Collection<Seed>>();
		error_seeds.put(this.getGroupId(), new HashSet<Seed>());
		
		for (Seed seed : seeds) {

			try{
				if(seed == null) continue;
				
				SeedBeanDefinition seedBD = beanFactory.lookupSeedBeanDefinition(seed.getClass());
				Analyzer analyzer = findAnalyzer(seedBD);
				if(analyzer == null)
					throw new Exception("analyzer is null ,can't analyze the seed");
				
				Map<Class<? extends Seed>,Collection<Seed>> seedsMap = analyzer.analyze(seed);
				processNewSeed(seedsMap);
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("seed:"+JacksonUtils.toJson(seed)+",error cause{}",e.getMessage());
				error_seeds.get(this.getGroupId()).add(seed);
				continue;
			}
		}
		
		return error_seeds.get(this.getGroupId()).isEmpty()?null:error_seeds;
	}
	/**
	 * 将seed直接包装成Task直接放入线程池立即执行
	 * @param seedBD
	 * @param seeds
	 */
	private void submitToExecutor(SeedBeanDefinition seedBD,Collection<Seed> seeds)
	{
		String executorName = StringUtils.isBlank(seedBD.getExecutor())?ExecutorFactory.DEFAULT_POOL_EXECUTOR:seedBD.getExecutor();
		int split = seedBD.getSplit();
		
		/**
		 * 分组策略为每个任务执行split个seed
		 * 当split<=0的时候，即所有seed一个任务
		 * 当split>0则通过seed的数量/split算出一共有多少个任务
		 * 根据任务数量，循环将seed添加到各个任务里面
		 * 分组策略在@SeedAnalyzer的split属性
		 */
		if(split > 0)
		{
			Collection<SeedTask> tasks = new HashSet<SeedTask>();
			int task_num = seeds.size()%split==0?seeds.size()/split:(seeds.size()/split)+1;
			Seed[] seed_array = seeds.toArray(new Seed[seeds.size()]);
			
			for(int i=0;i<task_num;i++)
			{
				Collection<Seed> _seeds = new HashSet<Seed>();
				for(int j=0;j<split;j++)
				{
					if(((i*split)+j)<seed_array.length)_seeds.add(seed_array[(i*split)+j]);
				}
				
				SeedTask task = new SeedTask(_seeds,this.getGroupId());
				tasks.add(task);
			}
			executorFactory.getExecutor(executorName).addTasks(tasks);
		}
		else
		{
			SeedTask task = new SeedTask(seeds,this.getGroupId());
			executorFactory.getExecutor(executorName).addTask(task);
		}
	}
	/**
	 * 将seed加入队列，定时执行
	 * @param seedClass
	 * @param seeds
	 */
	private void submitToScheduleExecutor(SeedBeanDefinition seedBD,Collection<Seed> seeds)
	{
		String queue = seedBD.getQueue();
		for(Seed  seed : seeds)
			ExecutorFactory.addSeedToQueue(queue, seed);
	}
	
	/**
	 * 找到该Seed的解析类
	 * @param seedBD
	 * @return
	 * @throws Exception
	 */
	private Analyzer findAnalyzer(SeedBeanDefinition seedBD) throws Exception
	{
		if(seedBD == null)
		{
			logger.warn("SeedBeanDefinition is null ,can't sow the seed");
			return null;
		}
		
		Analyzer analyzer = (Analyzer)beanFactory.getBean(seedBD.getAnalyzer());
		
		return analyzer;
	}

	public Collection<Seed> getSeeds() {
		return seeds;
	}

	public void setSeeds(Collection<Seed> seeds) {
		this.seeds = seeds;
	}

	public String getGroupId() {
		return groupId;
	}
	
}
