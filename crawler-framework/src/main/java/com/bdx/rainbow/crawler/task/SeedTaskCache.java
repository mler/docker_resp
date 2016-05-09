package com.bdx.rainbow.crawler.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.crawler.utils.JacksonUtils;

public class SeedTaskCache {

	private static Logger logger = LoggerFactory.getLogger(SeedTaskCache.class);
	
	private static Collection<String> keywordSet = new HashSet<String>();
	
	private static Map<Class<? extends Seed>,List<Seed>> seedTaskCache = new HashMap<Class<? extends Seed>, List<Seed>>();

	public static void addSeed(Seed seed)
	{
		try{
			if(seedTaskCache.get(seed.getClass()) == null)
				seedTaskCache.put(seed.getClass(), Collections.synchronizedList(new ArrayList<Seed>()));
			
			seedTaskCache.get(seed.getClass()).add(seed);
			logger.debug("Cache["+seed.getClass().getName()+"] size:"+seedTaskCache.get(seed.getClass()).size());
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		}
		
	}
	
	public static Collection<Seed> getNext(Class<? extends Seed> clazz,int i)
	{
		List<Seed> seed_source = seedTaskCache.get(clazz);
		
		List<Seed> seeds = new CopyOnWriteArrayList<Seed>(seed_source.subList(0, i-1));
		if(seeds.isEmpty() == false)
			seedTaskCache.get(clazz).removeAll(seeds);
		try {
			logger.debug("return cache seeds:"+JacksonUtils.toJson(seeds));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return seeds;
	}

	public static void addKeyWord(String keyword)
	{
		keywordSet.add(keyword);
	}
	
	public static String getNextKeyWord()
	{
		if(keywordSet == null || keywordSet.isEmpty())
			return null;
		Set<String> keywords = new CopyOnWriteArraySet<String>(keywordSet);
		String keyword = keywords.iterator().next();
		keywordSet.remove(keyword);
		return keyword;
	}
	
	
	
}
