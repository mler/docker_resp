package com.bdx.rainbow.crawler.pool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdx.rainbow.crawler.bean.BeanFactory;
import com.bdx.rainbow.crawler.bean.BeanFactoryAware;
import com.bdx.rainbow.crawler.event.ExecutorEvent;
import com.bdx.rainbow.crawler.event.ExecutorEventListener;
import com.bdx.rainbow.crawler.monitor.StatInfo;
import com.bdx.rainbow.crawler.pool.ThreadPoolExecutorFactory.PoolType;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.crawler.task.SeedTask;
import com.bdx.rainbow.crawler.utils.JacksonUtils;

/**
 * 自定义线程执行者
 * @author mler
 * @2016年4月16日
 */
public abstract class Executor implements BeanFactoryAware {
	
	private final static Logger logger = LoggerFactory.getLogger(Executor.class);

	/**
	 * ExecutorEvent 时间监听器
	 */
	private List<ExecutorEventListener> listeners = new ArrayList<ExecutorEventListener>();

	/** 用于计算种子线程池中存货的线程 **/
	protected AtomicLong 				thread_submit = new AtomicLong(0);
//	
	protected AtomicLong 				thread_error = new AtomicLong(0);//http请求连续出错次数
//	
	/** 用于计算种子线程池中存货的线程 **/
	protected AtomicLong 				thread_completed = new AtomicLong(0);
	
	/** 多线程处理的completionService **/
	protected CompletionService<Object> 	completionService;
	/** bean定义以及构建工厂 **/
	protected BeanFactory beanFactory = BeanFactory.instance();
	/** 线程池工厂 **/
	protected ThreadPoolExecutorFactory poolFactory = ThreadPoolExecutorFactory.INSTANCE;
	/** 线程池名称 **/
	private String poolName = ThreadPoolExecutorFactory.DEFAULT_POOL_NAME;
	/** 线程执行器 **/
	protected ThreadPoolExecutor executor;
	
	protected Map<Object,AtomicReference<StatInfo>> statInfoMap = new ConcurrentHashMap<Object, AtomicReference<StatInfo>>(0);;
	/**  **/
	protected String name;
	
	protected Object key;
	
	protected Thread shutdownHook; 
	
	public Executor(final String name) throws Exception {
		super();
		this.name = name;
		this.executor = poolFactory.getExecutorInstance(poolName,PoolType.COMMON,3);
		completionService = new ExecutorCompletionService<Object>(executor,new LinkedBlockingQueue<Future<Object>>());
		registerShutdownHook();
	}
	
	public Executor(final String name,final String poolName,PoolType type) throws Exception {
		super();
		this.poolName = poolName; 
		this.name = name;
		this.executor = poolFactory.getExecutorInstance(this.poolName,type,3);
		completionService = new ExecutorCompletionService<Object>(executor,new LinkedBlockingQueue<Future<Object>>());
		registerShutdownHook();
	}
	
	/**
	 * 添加seed任务，由于在dealSeed中可能会新增新的
	 * @param task
	 */
	public synchronized void addTask(SeedTask task)
	{
		completionService.submit(task);
		thread_submit.addAndGet(1);
		try {
			logger.info("有新任务提交，总数["+thread_submit+"] seed："+JacksonUtils.toJson(task));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 所有的添加seed任务组。
	 * @param seed_collection
	 */
	public void addTasks(Collection<SeedTask> tasks)
	{
		//由于dealSeed中会对seeds做remove操作，因此需要加锁
		if(tasks ==  null || tasks.isEmpty())
			return;
		
		for(SeedTask task : tasks)
			addTask(task);
	}
	
	/**
	 * 启动线程池前，进行部分处理
	 * @throws Exception
	 */
	public void resetParameter()
	{
		thread_submit = new AtomicLong(0);
		thread_completed = new AtomicLong(0);
		thread_error = new AtomicLong(0);
	}
	
	/**
	 * 虚拟机关闭前的现场清理
	 * @throws Exception
	 */
	public abstract void cleanBeforeShutdown() ;
	
	/**
	 * 开始处理启动线程池处理，当处理完毕以后会关闭
	 * @param tasks
	 * @throws Exception  
	 */
	@SuppressWarnings("unchecked")
	public void restart() throws Exception
	{
		try{
			ExecutorEvent event = new ExecutorEvent(this);
			if(listeners != null || listeners.isEmpty() == false)
			{
				for(ExecutorEventListener listener : listeners)
					listener.beforeStartup(event);
			}
		}
		catch(Exception e)
		{
			logger.error("listener beforeStartup error,{}",e);
		}
		
		do
		{
			try {
				Object error_seeds =  completionService.take().get();
				/** 返回出错的seed的 **/
				if(error_seeds != null && error_seeds instanceof Map && ((Map<String,Collection<Seed>>)error_seeds).isEmpty() == false)
				{
					Map<String,Collection<Seed>> seeds = (Map<String,Collection<Seed>>)error_seeds;
					for(String groupId:seeds.keySet())
					{
						SeedTask error_task = new SeedTask(seeds.get(groupId),groupId);
						addTask(error_task);
						
						long thread_error = updateStatInfo(groupId,"thread_error");
						
						logger.debug("第"+thread_error+"出错任务:"+JacksonUtils.toJson(error_seeds));
					}
				}
//				log.debug("["+thread_completed.get()+"],共"+(pages == null?"0":((Collection)pages).size())+"个 :"+JacksonUtils.toJson(pages));
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				//无论任务是成功还是失败，将完成数量减1，失败的任务会重新发起新的任务。
				thread_completed.addAndGet(1);
			}
			logger.info("["+name+"][线程提交:"+thread_submit.get()+" 已经完成:"+thread_completed.get()+"  错误:"+thread_error.get()+"]");
		}
		while(thread_submit.get() > thread_completed.get());
		
		
		try{
			ExecutorEvent event = new ExecutorEvent(this);
			if(listeners != null || listeners.isEmpty() == false)
			{
				for(ExecutorEventListener listener : listeners)
					listener.afterCompleted(event);
			}
		}
		catch(Exception e)
		{
			logger.error("listener beforeStartup error,{}",e);
		}
	}
	
	private long updateStatInfo(String groupId, String prop)
	{
		long returnValue = 0 ;
		try{
			AtomicReference<StatInfo> reference = statInfoMap.get(groupId);
			StatInfo expect = reference.get();
			StatInfo update = null;
			do
			{
				update = (StatInfo) BeanUtils.cloneBean(expect);
				returnValue = (long)PropertyUtils.getProperty(update, prop)+1;
				PropertyUtils.setProperty(update, prop, returnValue);
			}
			while(!reference.compareAndSet(expect, update));
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("statinfo update fail");
		}
		
		return returnValue;
	}
	
	/**
	 * 对外开放的接口，线程池执行
	 * @throws Exeption
	 */
	public abstract void startup() throws Exception;
	
	/**
	 * 注册一个定时执行 任务
	 * @param command
	 * @param time
	 * @throws Exception
	 */
	public void registerScheduleTask(Runnable command,long time) throws Exception
	{
		if(executor instanceof ScheduledThreadPoolExecutor == false)
			throw new Exception("executor is not a ScheduledThreadPoolExecutor instance");
		
		if(time<=0)
			time = 10;
		((ScheduledThreadPoolExecutor)executor).scheduleWithFixedDelay(command, 3000, time, TimeUnit.MILLISECONDS);
	}

	public void registerShutdownHook() {
		if (this.shutdownHook == null) {
			// No shutdown hook registered yet.
			this.shutdownHook = new Thread() {
				@Override
				public void run() {
					cleanBeforeShutdown();
					shutDown();
				}
			};
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		}
	}
	
	/**
	 * 当虚拟机关闭时，现场清理
	 */
	public void shutDown()
	{
		if(executor == null)
			return;
			
		try{
			
			executor.shutdown();
			if(executor.awaitTermination(60, TimeUnit.SECONDS) == false){  
				logger.debug("["+this+"]所有的子线程都结束了，关闭线程池已经关闭！");  
	        }else {
				logger.debug("["+this+"]关闭线程池,关闭线程超时");
				executor.shutdownNow();
			}
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
			logger.error("["+this+"]关闭失败，error:{}",e.getMessage());
		}
	}
	
	public void registerStatInfo(Object key,StatInfo statInfo)
	{
		if(statInfo == null)
		{
			logger.warn("statinfo is null,can't register");
		}
		
		statInfoMap.put(key, new AtomicReference<StatInfo>(statInfo));
	}
	
	public void addListener(ExecutorEventListener listener)
	{
		if(listener == null)
			return;
		
		logger.debug("Executor["+name+"] add Listener :"+listener.getClass().getName());
		listeners.add(listener);
	}
	
	public void addListeners(Collection<ExecutorEventListener> listeners)
	{
		if(listeners == null || listeners.isEmpty())
			return;
		
		for(ExecutorEventListener listener : listeners)
		{
			addListener(listener);
		}
	}
	
	public void removeListener(ExecutorEventListener listener)
	{
		if(listener == null)
			return;
		
		logger.debug("Executor["+name+"] remove Listener :"+listener.getClass().getName());
		listeners.remove(listener);
	}

	public String getName() {
		return name;
	}

	public void setBeanFactory(BeanFactory beanFactory){
		this.beanFactory = beanFactory;
	}
}