package com.bdx.rainbow.gds.seed;

import com.bdx.rainbow.crawler.annotation.SeedAnalyzer;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.gds.analyzer.GdsSupplierAnalyzer;

@SeedAnalyzer(analyzer=GdsSupplierAnalyzer.class,queue="testQueue")
public class GdsSupplierSeed implements Seed {
	
	private String url;
	
	public GdsSupplierSeed(String url) {
		super();
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public SeedType getType() {
		return SeedType.HTTP;
	}
	
}
