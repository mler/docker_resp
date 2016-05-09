package com.bdx.rainbow.crawler.task;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;

import com.bdx.rainbow.crawler.seed.Seed;

/**
 * 处理种子的线程
 * @author mler
 * 
 * T为传入的Seed类型
 * V为返回类型
 * @param <V>
 */
public class SeedTask implements Callable<Object>,Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3515835488104946482L;

	private Collection<Seed> seeds;
	
	private  String groupId;
	
	public SeedTask(Collection<Seed> seeds, String groupId) {
		super();
		this.seeds = seeds;
		this.groupId = groupId;
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
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	@Override
	public Object call() throws Exception {
		
		return new SeedProcessor(seeds, groupId).process();
		
	}
	
}
