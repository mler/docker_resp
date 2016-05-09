package com.bdx.rainbow.crawler.lisener;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.bdx.rainbow.crawler.bean.BeanFactory;
import com.bdx.rainbow.crawler.bean.Initable;
import com.bdx.rainbow.crawler.pool.ExecutorFactory;

public class CrawlerContextLoaderLisener extends ContextLoader implements ServletContextListener {

	private Logger logger = LoggerFactory.getLogger(CrawlerContextLoaderLisener.class);
	
	private String creator = BeanFactory.SPRING_CREATOR;
	
	private String[] packages ={"com.bdx.rainbow.mapp.core"};
	
	public CrawlerContextLoaderLisener()
	{
		
	}
	
	public CrawlerContextLoaderLisener(WebApplicationContext context) {
		super(context);
	}


	/**
	 * Initialize the root web application context.
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		
		try{
			
			BeanFactory beanFactory = BeanFactory.instance();
			beanFactory.initializeContext(packages, creator);
			/**
			 * 先初始化spring上下文，首先初始化BeanFactory，
			 * 如果spring方式则使用SpringBeanFactory，
			 * 否则通过mapp自己的MappBeanFacory，
			 * 然后初始化mapp上下文
			 */
			if(BeanFactory.SPRING_CREATOR.equals(creator))
			{
//				initWebApplicationContext(event.getServletContext());
				ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
//				if(context == null)
//					initWebApplicationContext(event.getServletContext());
				beanFactory.setApplicationContext(context);
			}
			
			logger.info("contextLoader initialized.............");
//			Map<String,ExecutorEventListener> listenerMap = beanFactory.getBeansOfType(ExecutorEventListener.class);
//			
//			ExecutorFactory factory = ExecutorFactory.INSTANCE;
//	    	/** 普通的任务线程池 **/
//			Executor pool_executor = new GdsExecutor(ExecutorFactory.DEFAULT_POOL_EXECUTOR,ThreadPoolExecutorFactory.DEFAULT_POOL_NAME,PoolType.COMMON);
//			factory.registerExecutor(pool_executor);
//	    	/** 定时任务线程池 **/
//	    	Executor schedule_executor = new GdsExecutor(ExecutorFactory.DEFAULT_SCHEDULE_EXECUTOR,ThreadPoolExecutorFactory.DEFAULT_SCHEDULE_NAME,PoolType.SCHEDULE);
//	    	factory.registerExecutor(schedule_executor);
//	    	/** 增加监听 ***/
//	    	if(listenerMap != null && listenerMap.isEmpty()==false)
//	    	{
//	    		pool_executor.addListeners(listenerMap.values());
//	    		schedule_executor.addListeners(listenerMap.values());
//	    	}
//	    	
//	    	pool_executor.startup();
			
	    	/** 初始化 **/
	    	Map<String,Initable> initBeanMap = beanFactory.getBeansOfType(Initable.class);
			if(initBeanMap != null && initBeanMap.isEmpty() == false)
			{
				for(Initable initbean : initBeanMap.values())
				{
					initbean.init();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.error("Crawler Context init failed,cause:"+e.getMessage());
		}
	}


	/**
	 * Close the root web application context.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		
		/**
		 * 如果有ScheduleExecutor则在此关闭，如果是tomcat直接关闭不会触发shutdownhook
		 */
		ExecutorFactory.INSTANCE.destory();
		
		closeWebApplicationContext(event.getServletContext());
		Enumeration<String> attrNames = event.getServletContext().getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = attrNames.nextElement();
			if (attrName.startsWith("org.springframework.")) {
				Object attrValue = event.getServletContext().getAttribute(attrName);
				if (attrValue instanceof DisposableBean) {
					try {
						((DisposableBean) attrValue).destroy();
					}
					catch (Throwable ex) {
						logger.error("Couldn't invoke destroy method of attribute with name '" + attrName + "'", ex);
					}
				}
			}
		}
	}
	

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setPackages(String[] packages) {
		this.packages = packages;
	}


}
