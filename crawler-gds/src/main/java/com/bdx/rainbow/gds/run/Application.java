package com.bdx.rainbow.gds.run;

/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.bdx.rainbow.crawler.bean.BeanFactory;
import com.bdx.rainbow.crawler.event.ExecutorEventListener;
import com.bdx.rainbow.crawler.lisener.CrawlerContextLoaderLisener;
import com.bdx.rainbow.crawler.pool.Executor;
import com.bdx.rainbow.crawler.pool.ExecutorFactory;
import com.bdx.rainbow.crawler.pool.ThreadPoolExecutorFactory;
import com.bdx.rainbow.crawler.pool.ThreadPoolExecutorFactory.PoolType;
import com.bdx.rainbow.gds.configuration.CrawlerConfig;
import com.bdx.rainbow.gds.executor.GdsExecutor;
@Configuration
@ComponentScan({"com.bdx.rainbow"})
@EnableAutoConfiguration
@ImportResource("classpath:applicationContext.xml")
public class Application extends WebMvcConfigurerAdapter {
	
    public static void main(String[] args) throws IOException, Exception {
    	
    	ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    	
//    	CrawlerConfig.ifProxy = true;
//    	
//    	BeanFactory beanFactory = BeanFactory.instance();
//		Map<String,ExecutorEventListener> listenerMap = beanFactory.getBeansOfType(ExecutorEventListener.class);
//		ExecutorFactory factory = ExecutorFactory.INSTANCE;
//    	/** 普通的任务线程池 **/
//		Executor pool_executor = new GdsExecutor(ExecutorFactory.DEFAULT_POOL_EXECUTOR,ThreadPoolExecutorFactory.DEFAULT_POOL_NAME,PoolType.COMMON);
//		factory.registerExecutor(pool_executor);
//    	/** 定时任务线程池 **/
//    	Executor schedule_executor = new GdsExecutor(ExecutorFactory.DEFAULT_SCHEDULE_EXECUTOR,ThreadPoolExecutorFactory.DEFAULT_SCHEDULE_NAME,PoolType.SCHEDULE);
//    	factory.registerExecutor(schedule_executor);
//    	/** 增加监听 ***/
//    	if(listenerMap != null && listenerMap.isEmpty()==false)
//    	{
//    		pool_executor.addListeners(listenerMap.values());
//    		schedule_executor.addListeners(listenerMap.values());
//    	}
//    	
//    	pool_executor.startup();
    }
	
	@Bean
    public ServletListenerRegistrationBean<CrawlerContextLoaderLisener> servletRegistrationBean() {
		String[] packages = {"com.bdx.rainbow"};
		CrawlerContextLoaderLisener contextLisener = new CrawlerContextLoaderLisener();
		contextLisener.setCreator("spring");
		contextLisener.setPackages(packages);
        return new ServletListenerRegistrationBean<CrawlerContextLoaderLisener>(contextLisener);
    }

}