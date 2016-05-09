package com.bdx.rainbow.crawler.bean;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import com.bdx.rainbow.crawler.annotation.SeedAnalyzer;
import com.bdx.rainbow.crawler.utils.ClassUtils;

public class BeanFactory implements ApplicationContextAware {

	private Logger logger = LoggerFactory.getLogger(BeanFactory.class);
	
	private Map<Class<?>,SeedBeanDefinition> seedBeanDefinitionMap;
	
	private Map<Class<?>,Object> singleObjectMap;
	
	private Thread shutdownHook;//清理
	
	private String creator;
	
	private String[] packages; 
	
	private static BeanFactory beanFactory = new BeanFactory();
	
	public static final String SPRING_CREATOR = "spring";
	
	private ApplicationContext context;
	
	public static BeanFactory instance()
	{
		return beanFactory;
	}

	/** 注册Bean定义 **/
	public void registerSeedBeanDefinition(SeedBeanDefinition beanDefinition)
	{
		if(seedBeanDefinitionMap == null)
			seedBeanDefinitionMap = new ConcurrentHashMap<Class<?>, SeedBeanDefinition>();
			
		seedBeanDefinitionMap.put(beanDefinition.getSeedClass(),beanDefinition);
	}
	
	/** 从缓存中查找指定Class的Bean定义 **/
	public SeedBeanDefinition lookupSeedBeanDefinition(Class<?> seedClass)
	{
		if(seedClass == null || seedBeanDefinitionMap ==null)
			return null;
		
		return seedBeanDefinitionMap.get(seedClass);
	}
	
	/**
	 * 获取bean，如果类的构造工厂为spring则通过spring构造该类，否则则通过newInstance方法进行构造
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public Object getBean(Class<?> clazz) throws Exception
	{
		if(singleObjectMap == null)
			singleObjectMap = new ConcurrentHashMap<Class<?>, Object>();
		
		Object singleBean  = SPRING_CREATOR.equals(creator)?context.getBean(clazz):clazz.newInstance();
			
		return singleBean;
	}
	
	public<T> Map<String,T> getBeansOfType(Class<T> clazz)
	{
		if(SPRING_CREATOR.equals(creator))
		{
			return context.getBeansOfType(clazz);
		}
		else
		{
			logger.warn("BeanFactory.getBeanByType(Class<T> clazz) is only support for creator = 'spring'");
			return null;
		}
	}
	
	private void contextLoadSeed(Class<?> clazz) throws Exception
	{
		SeedAnalyzer seedAnalyzer =AnnotationUtils.findAnnotation(clazz, SeedAnalyzer.class);
		if(seedAnalyzer == null) 
			return;
		
		Class<?> analyzer = (Class<?>)AnnotationUtils.getValue(seedAnalyzer, "analyzer");
		String executor = AnnotationUtils.getValue(seedAnalyzer, "executor").toString();
		Integer split = Integer.valueOf(AnnotationUtils.getValue(seedAnalyzer, "split").toString());
		
		boolean schedule = Boolean.valueOf(AnnotationUtils.getValue(seedAnalyzer, "schedule").toString());
		String queue = AnnotationUtils.getValue(seedAnalyzer, "queue").toString();
		
		SeedBeanDefinition beanDefinition = new SeedBeanDefinition();
		beanDefinition.setAnalyzer(analyzer);
		beanDefinition.setExecutor(executor);
		beanDefinition.setSeedClass(clazz);
		beanDefinition.setSplit(split);
		beanDefinition.setQueue(queue);
		beanDefinition.setSchedule(schedule);
		
		registerSeedBeanDefinition(beanDefinition);
		
		logger.debug(clazz.getName()+" beanDefinition register success");
	}
	
	public void initializeContext(String[] packages,String creator) throws Exception
	{	
		logger.info("====================== init parameters packages:"+packages+",creator:"+creator+"==================");
		
		this.creator = creator;
		this.packages = packages;

		logger.info("====================== scan packages:"+packages+"==================");
		
		if(packages == null || packages.length == 0)
			return;
		
		Collection<Class<?>> clazzes = new HashSet<Class<?>>();
		
		for(String p : packages)
			clazzes.addAll(ClassUtils.getClasssFromPackage(p));
		
    	for(Class<?> clazz:clazzes) {
    		contextLoadSeed(clazz);
    	}
    	
    	registerShutdownHook();
    	
    	logger.info("======================beanFactory initialized=================");
	}
	
	private void destroyBeaFactory()
	{
		logger.info("=================beanFactory 清理现场================");
		seedBeanDefinitionMap = null;
		singleObjectMap = null;
		logger.info("=================beanFactory 清理完毕================");
	}
	
	public void registerShutdownHook() {
		if (this.shutdownHook == null) {
			// No shutdown hook registered yet.
			this.shutdownHook = new Thread() {
				@Override
				public void run() {
					destroyBeaFactory();
				}
			};
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.context = context;
	}

	public static void main(String[] args) {
		Set<Class<?>> clazzs = ClassUtils.getClasssFromPackage("com.bdx.rainbow");//
		for (Class<?> clazz : clazzs) {
			System.out.println(clazz.getName());
		}

		// clazzs = classUtil.getClasssFromJarFile("Jar文件的路径", "Jar文件里面的包路径");
		// for (Class clazz : clazzs) {
		// logger.debug(clazz.getName());
		// }
		//
		// classUtil.getStream("Jar文件的路径", "Jar文件总的一个具体文件的路径");
	}
}
