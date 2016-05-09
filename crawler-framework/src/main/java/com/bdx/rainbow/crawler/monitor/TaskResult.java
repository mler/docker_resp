package com.bdx.rainbow.crawler.monitor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.bdx.rainbow.crawler.seed.Seed;

public class TaskResult implements Serializable {

	private String groupId;
	
	private long success;
	
	private long total;
	
	private long fail;
	
	private Map<String,Collection<Seed>> errorSeeds;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public long getSuccess() {
		return success;
	}

	public void setSuccess(long success) {
		this.success = success;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getFail() {
		return fail;
	}

	public void setFail(long fail) {
		this.fail = fail;
	}

	public Map<String, Collection<Seed>> getErrorSeeds() {
		return errorSeeds;
	}

	public void setErrorSeeds(Map<String, Collection<Seed>> errorSeeds) {
		this.errorSeeds = errorSeeds;
	}
	
}
