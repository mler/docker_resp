package com.bdx.rainbow.gds.seed;

import java.net.URLEncoder;

import com.bdx.rainbow.crawler.annotation.SeedAnalyzer;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.gds.analyzer.GdsListPageAnalyzer;

@SeedAnalyzer(analyzer=GdsListPageAnalyzer.class,split=1,queue="testQueue")
public class GdsListPageSeed implements Seed {

	private static final long serialVersionUID = -2815585218042094279L;

	private final String baseUrl="http://search.anccnet.com/searchResult2.aspx";
	
	private String keyword;
	
	private int rPage;
	
	public String getBaseUrl() {
		return baseUrl;
	}
	
	public String getUrl() throws Exception
	{
		return baseUrl+"?keyword="+URLEncoder.encode(keyword,"gbk")+"&rPage="+rPage;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getrPage() {
		return rPage;
	}

	public void setrPage(int rPage) {
		this.rPage = rPage;
	}

	@Override
	public SeedType getType() {
		return SeedType.HTTP;
	}

//	public static final void main(String[] args) throws Exception
//	{
//		GdsListPageSeed seed = new GdsListPageSeed();
//		seed.setKeyword("咖啡");
//		seed.setrPage(1);
//		
//		System.out.println(seed.getUrl());
//	}
	
}
