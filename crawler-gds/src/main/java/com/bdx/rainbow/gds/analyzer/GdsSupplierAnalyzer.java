package com.bdx.rainbow.gds.analyzer;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
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
import com.bdx.rainbow.gds.entity.ProductorGds;
import com.bdx.rainbow.gds.mapper.ProductorGdsMapper;
import com.bdx.rainbow.gds.seed.GdsSupplierSeed;

@Component
public class GdsSupplierAnalyzer implements Analyzer {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ProductorGdsMapper productorGdsMapper;
	
	@Override
	public Map<Class<? extends Seed>, Collection<Seed>> analyze(Seed seed) throws Exception {
		
		GdsSupplierSeed gdsSupplierSeed = (GdsSupplierSeed)seed;
		
		if(gdsSupplierSeed == null || StringUtil.isBlank(gdsSupplierSeed.getUrl()) )
			throw new Exception("解析参数有误，请检查");
		
		Map<String,String> header = new HashMap<String, String>();
        header.put(HttpHeaders.HOST, "www.anccnet.com");
        header.put(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate,sdch");
        header.put("Proxy-Connection", "keep-alive");
        header.put(HttpHeaders.USER_AGENT,"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
        header.put(HttpHeaders.REFERER, "http://search.anccnet.com/searchResult2.aspx");

        String url = gdsSupplierSeed.getUrl();
        
        String html="";
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
		
		Collection<ProductorGds> suppliers = gdsAnalyzeByHtml(html);
		if(suppliers == null || suppliers.isEmpty())
			return null;
		
		for(ProductorGds supplier : suppliers)
		{
			supplier.setUpdateUrl(gdsSupplierSeed.getUrl());
			supplier.setUpdateTime(new Timestamp(System.currentTimeMillis()));
			
			if(supplier != null)
			{
				ProductorGds old = productorGdsMapper.selectByPrimaryKey(supplier.getpCode());
				if(old == null)
				{
					supplier.setCreateTime(supplier.getUpdateTime());
					productorGdsMapper.insertSelective(supplier);
				}
				else
				{
					continue;
//					productorGdsMapper.updateByPrimaryKeySelective(supplier);
				}
			}
		}
	
		return null;
	}

	private Collection<ProductorGds> gdsAnalyzeByHtml(String html) throws Exception
	{
		
		if(StringUtil.isBlank(html))
			throw new Exception("生产厂商信息有误，解析失败");
		
		Document doc = Jsoup.parse(html,"gbk");
		
		Elements tr_elements = doc.select("div#mainDiv > table:eq(0) > tbody:eq(0) > tr");
		
		if(tr_elements == null || tr_elements.isEmpty())
			return null;
		
		Map<String,String> pMap= new HashMap<String, String>();
		
		for(Element e : tr_elements)
		{
			Elements td = e.select("td");
			String key = td.get(0).html().replaceAll(":", "").replaceAll("：", "");
//			System.out.println(td.get(0).html()+":"+td.get(1).text().trim());
			pMap.put(key.trim(),td.get(1).text().trim());
		}
		
		Collection<ProductorGds> productors = new HashSet<ProductorGds>();
		if(pMap.get("厂商识别代码").length() > 10)
		{
			String pCode = pMap.get("厂商识别代码");
			String[] pCodes = pCode.split(" ");
			
			for(String code : pCodes)
			{
				ProductorGds p = new ProductorGds();
				p.setAddress(pMap.get("注册地址（中文）"));
				p.setEaddress(pMap.get("注册地址（英文）"));
				p.setEname(pMap.get("企业名称（英文）"));
				p.setName(pMap.get("企业名称（中文）"));
				p.setpCode(code);
				p.setPostcode(pMap.get("注册地址邮政编码"));
			
			
				logger.info("厂家："+JacksonUtils.toJson(p));
			
				productors.add(p);
			}
			
		}
		else
		{
		
			ProductorGds p = new ProductorGds();
			p.setAddress(pMap.get("注册地址（中文）"));
			p.setEaddress(pMap.get("注册地址（英文）"));
			p.setEname(pMap.get("企业名称（英文）"));
			p.setName(pMap.get("企业名称（中文）"));
			p.setpCode(pMap.get("厂商识别代码"));
			p.setPostcode(pMap.get("注册地址邮政编码"));
			logger.info("厂家："+JacksonUtils.toJson(p));
			
			productors.add(p);
		}
		
		
		
		
		return productors;
	}
	
//	public final static void main(String[] args) throws Exception
//	{
//		String catalogString = "巧克力|糖|核桃|瓜子|肉铺|豆腐|鱼片|饼干|薯|乐事|上好佳|曲奇|话梅|橄榄|鸡爪|矿泉水|海苔|肉松|红枣|鸭脖|牛肉|鱿鱼|白酒|红酒|啤酒|朗姆酒|黄酒|葡萄酒|西湖龙井|白茶|绿茶|大红袍|铁观音|普洱茶|玫瑰花|柠檬|芒果|果冻|菊花|红茶|咖啡|可乐|美年达|芬达|雪碧|醒目|王老吉|龟苓膏|肠|奶茶|麦片|酱油|味精|榨菜|奶粉|果汁|油|米|银耳|桂圆|香肠|木耳|菇|酱|冰激凌|水饺|馄饨|酸奶|丸|面|糖|寿司|玉米|牛排|车厘子|蛋|鸡|鸭|鱼";
//   	 	String[] catalog = catalogString.split("\\|");
//   	 	System.out.println(catalog.length);
//	}
	
}
