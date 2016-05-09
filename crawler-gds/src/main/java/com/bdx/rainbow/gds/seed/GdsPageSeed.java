package com.bdx.rainbow.gds.seed;

import java.net.URLEncoder;

import com.bdx.rainbow.crawler.annotation.SeedAnalyzer;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.gds.analyzer.GdsPageAnalyzer;

@SeedAnalyzer(analyzer=GdsPageAnalyzer.class,split=1,queue="testQueue")
public class GdsPageSeed implements Seed {

	private static final long serialVersionUID = -2815585218042094279L;

	private final String baseUrl="http://search.anccnet.com/searchResult2.aspx";
	
	private String keyword;
	
	public String getBaseUrl() {
		return baseUrl;
	}
	
	public String getUrl() throws Exception
	{
		return baseUrl+"?keyword="+URLEncoder.encode(keyword,"gbk");
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	@Override
	public SeedType getType() {
		return SeedType.HTTP;
	}
	
}
