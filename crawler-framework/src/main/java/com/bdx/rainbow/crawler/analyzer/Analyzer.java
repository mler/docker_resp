package com.bdx.rainbow.crawler.analyzer;

import java.util.Collection;
import java.util.Map;

import com.bdx.rainbow.crawler.seed.Seed;

/**
 * 解析器，每个种子对应一个解析器，用来处理Seed
 * @author mler
 * @2016年4月28日
 */
public interface Analyzer {
	public Map<Class<? extends Seed>,Collection<Seed>> analyze(Seed seed) throws Exception;
}
