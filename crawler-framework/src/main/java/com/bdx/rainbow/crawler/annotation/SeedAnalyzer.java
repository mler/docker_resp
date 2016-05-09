package com.bdx.rainbow.crawler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bdx.rainbow.crawler.analyzer.Analyzer;
import com.bdx.rainbow.crawler.pool.ExecutorFactory;

@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.TYPE)
public @interface SeedAnalyzer{

	public Class<? extends Analyzer> analyzer() default Analyzer.class;
	
	public String executor() default ExecutorFactory.DEFAULT_POOL_EXECUTOR;
	
	public int split() default 1;
	
	public boolean schedule() default false;
	
	public String queue() default "";
	
}
