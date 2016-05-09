package com.bdx.rainbow.crawler.monitor;

import java.io.Serializable;

/**
 * 统计信息实体
 * @author mler
 * @2016年4月16日
 */
public class StatInfo implements Serializable {

	private String groupId;
	
	private long success;
	
	private long total;
	
	private long fail;
	
	private long startTime;
	
	private long endTime;

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

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public long getFail() {
		return fail;
	}

	public void setFail(long fail) {
		this.fail = fail;
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

}
