package com.bdx.rainbow.gds.analyzer;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bdx.rainbow.common.util.HttpClientUtil;
import com.bdx.rainbow.crawler.analyzer.Analyzer;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.crawler.utils.JacksonUtils;
import com.bdx.rainbow.gds.configuration.CrawlerConfig;
import com.bdx.rainbow.gds.entity.Pgds;
import com.bdx.rainbow.gds.executor.GdsExecutor;
import com.bdx.rainbow.gds.mapper.PgdsMapper;
import com.bdx.rainbow.gds.seed.GdsListPageSeed;
import com.bdx.rainbow.gds.seed.GdsProductSeed;
import com.bdx.rainbow.gds.seed.GdsSupplierSeed;

@Component
public class GdsListPageAnalyzer implements Analyzer {

	private Logger logger = LoggerFactory.getLogger(GdsListPageAnalyzer.class);
	
	private final static String  SUPPLIER_URL = "http://www.anccnet.com/supplierDetails.aspx";
	
	@Autowired
	private PgdsMapper pgdsMapper;
	
	@Override
	public Map<Class<? extends Seed>, Collection<Seed>> analyze(Seed seed) throws Exception {

        Map<String,String> header = new HashMap<String, String>();
        header.put(HttpHeaders.HOST, "search.anccnet.com");
        header.put(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate,sdch");
        header.put("Proxy-Connection", "keep-alive");
        header.put(HttpHeaders.USER_AGENT,"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
        header.put(HttpHeaders.REFERER, "http://www.gds.org.cn/");

        String url = ((GdsListPageSeed) seed).getUrl();
        
        logger.debug("请求地址："+url);
        
        String html = "";
        if(CrawlerConfig.ifProxy)
        {
        	html = HttpClientUtil.getStringResponseByProxy(
				url, "get", header, null, null,
				"hzproxy.asiainfo.com", 8080, "utf-8");
        }
        else
        {
        	html = HttpClientUtil.getStringResponse(
				url, "get", header, null, null, "utf-8");
        }
        if(StringUtils.isBlank(html))
        	throw new Exception("无法打开地址："+url);
        	
//		logger.debug(html);
		
		Document doc = Jsoup.parse(html,"utf-8");
		
		String countPage = doc.select("div#myPager > table:eq(0) > tbody:eq(0) > tr:eq(0) > td:eq(0) > font:eq(1)").text();
		Integer page = Integer.valueOf(countPage);
		/** 第一页已经分析，从第二页开始取 **/
		for(int i=2;i<=page;i++)
		{
			GdsListPageSeed _seed = new GdsListPageSeed();
			_seed.setKeyword(((GdsListPageSeed) seed).getKeyword());
			_seed.setrPage(i);
		}
		
		
        Elements dls = doc.getElementsByTag("dl");

        Map<Class<? extends Seed>,Collection<Seed>> seedMap = new HashMap<Class<? extends Seed>, Collection<Seed>>();
        
        for (Element dl : dls) {
            
            Elements dts = dl.getElementsByTag("dt");
            Elements dds = dl.getElementsByTag("dd");

            for (int i = 0; i < dts.size(); i++) {
                if (dts.get(i).text().contains("商品条码"))
                {
                	String barcode = dds.get(i).select("a").text();
//                	logger.debug(barcode);
                	String gdsUrl = dds.get(i).select("a").attr("href");
                	GdsProductSeed pseed = new GdsProductSeed(barcode,gdsUrl);
                	
                	if(seedMap.get(GdsProductSeed.class) == null)
                		seedMap.put(GdsProductSeed.class, new HashSet<Seed>());
//                	seedMap.get(GdsProductSeed.class).add(pseed);
                }
                else if(dts.get(i).text().contains("发布厂家")) 
                {
                    String gdsUrl = dds.get(i).select("a").attr("href");
                    String f_id = gdsUrl.substring(gdsUrl.indexOf("id=")+3,gdsUrl.indexOf("&temp="));
                    gdsUrl = SUPPLIER_URL+"?f_id="+f_id;
                    
                    GdsSupplierSeed pseed = new GdsSupplierSeed(gdsUrl);
                    
                    if(seedMap.get(GdsSupplierSeed.class) == null)
                		seedMap.put(GdsSupplierSeed.class, new HashSet<Seed>());
                	seedMap.get(GdsSupplierSeed.class).add(pseed);
                }
            }
        }
        
        saveProduct(doc);
	
        return seedMap;
	}

	private void saveProduct(Document doc) throws Exception
	{
			Elements product_dls = doc.select("dl.p-info");
			Elements supplier_dls = doc.select("dl.p-supplier");
			
			for(int i=0;i<product_dls.size();i++)
			{
				Elements product_dds = product_dls.get(i).select("dd");
				Elements supplier_dds = supplier_dls.get(i).select("dd");
				Pgds goods = new Pgds();
				goods.setBrand(supplier_dds.get(0).text());
				goods.setProductor(supplier_dds.get(1).text());
				goods.setBarcode(product_dds.get(0).text());
				goods.setName(product_dds.get(1).text());
				goods.setSpec(product_dds.get(2).text());
				
				goods.setUpdateUrl(product_dds.get(0).select("a").attr("href"));
				goods.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			
				if(goods != null)
				{
					Pgds old = pgdsMapper.selectByPrimaryKey(goods.getBarcode());
					if(old == null)
					{
						goods.setCreateTime(goods.getUpdateTime());
						pgdsMapper.insertSelective(goods);
						logger.info("商品成功抓取个数:"+GdsExecutor.good_success.addAndGet(1)+",商品信息="+JacksonUtils.toJson(goods));
					}
					else 
					{
						logger.info("商品成功抓取个数:"+GdsExecutor.good_success.addAndGet(1)+",商品信息="+JacksonUtils.toJson(goods));
						continue;
	//					pgdsMapper.updateByPrimaryKeySelective(goods);
					}
				}
				
				logger.debug("商品成功抓取个数:"+GdsExecutor.good_success.addAndGet(1));
				
			}
		
	}
	
}

 
