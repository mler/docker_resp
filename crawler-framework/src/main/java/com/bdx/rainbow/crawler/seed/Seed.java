package com.bdx.rainbow.crawler.seed;

import java.io.Serializable;

/**
 * 定义seed类型
 * @author mler
 * @2016年4月8日
 */
public interface Seed extends Serializable {
	
	public enum SeedType
	{
		HTTP
	}
	
	public SeedType getType();
}
