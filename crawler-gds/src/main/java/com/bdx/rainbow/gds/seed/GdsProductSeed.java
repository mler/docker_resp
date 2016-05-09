package com.bdx.rainbow.gds.seed;

import com.bdx.rainbow.crawler.annotation.SeedAnalyzer;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.gds.analyzer.GdsProductAnalyzer;

/**
 * http请求的种子
 * @author mler
 * @2016年4月8日
 */
@SeedAnalyzer(analyzer=GdsProductAnalyzer.class,queue="testQueue")
public class GdsProductSeed implements Seed {
	
	private String barcode;
	
	private String url;
	
	public GdsProductSeed(String barcode,String url) {
		super();
		this.barcode = barcode;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	@Override
	public SeedType getType() {
		// TODO Auto-generated method stub
		return SeedType.HTTP;
	}
	
}