package com.bdx.rainbow.gds.analyzer;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.jsoup.Connection;
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
import com.bdx.rainbow.gds.entity.Pgds;
import com.bdx.rainbow.gds.mapper.PgdsMapper;
import com.bdx.rainbow.gds.seed.GdsProductSeed;

@Component
public class GdsProductAnalyzer implements Analyzer {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PgdsMapper pgdsMapper;
	
	@Override
	public Map<Class<? extends Seed>, Collection<Seed>> analyze(Seed seed) throws Exception {
		
		GdsProductSeed gdsProductSeed = (GdsProductSeed)seed;
		
		if(gdsProductSeed == null || StringUtil.isBlank(gdsProductSeed.getBarcode()) )
			throw new Exception("解析参数有误，请检查");
		
		
		Map<String,String> header = new HashMap<String, String>();
        header.put(HttpHeaders.HOST, "www.anccnet.com");
        header.put(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate,sdch");
        header.put("Proxy-Connection", "keep-alive");
        header.put(HttpHeaders.USER_AGENT,"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
        header.put(HttpHeaders.REFERER, "http://search.anccnet.com/searchResult2.aspx");

        String url = gdsProductSeed.getUrl();
        
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
		
		Pgds goods = gdsAnalyzeByHtml(gdsProductSeed.getBarcode(), html);
			 goods.setUpdateUrl(gdsProductSeed.getUrl());
			 goods.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		
		if(goods != null)
		{
			Pgds old = pgdsMapper.selectByPrimaryKey(gdsProductSeed.getBarcode());
			if(old == null)
			{
				goods.setCreateTime(goods.getUpdateTime());
				pgdsMapper.insertSelective(goods);
			}
			else 
				pgdsMapper.updateByPrimaryKeySelective(goods);
		}
		
		return null;
	}
	
	private Pgds gdsAnalyzeByHtml(String barcode,String html) throws Exception
	{
		if(StringUtil.isBlank(barcode) || StringUtil.isBlank(html))
			throw new Exception("商品信息有误，解析失败");
		String script = null;
		try{
			script = html.substring(html.indexOf("SetValue('Att"), html.indexOf("delNullRow();}"));
			script = script.replaceAll("&nbsp;", "");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logger.debug(html);
		}
        
        String[] arr = script.split(";");
        for (String s : arr) {
            if (s.trim().length() != 0) {
                String key = s.substring(s.indexOf("Att"), s.indexOf(",") - 1);
                String value = s.substring((s.indexOf(",'") + 2), s.indexOf("')"));

                html = html.replaceAll("<span id=\"" + key + "\"></span>", value);
            }
        }
		
		Document doc = Jsoup.parse(html,"gbk");
		
		Elements tr_elements = doc.select("table#productsInfo > tbody:eq(0) > tr");
		
		Map<String,String> pMap= new HashMap<String, String>();
		
		for(Element e : tr_elements)
		{
			Elements td = e.select("td");
//			logger.debug(td.get(0).html()+":"+td.get(1).text());
			pMap.put(td.get(0).html().trim(),td.get(1).text().trim());
		}
		
		
		Pgds p = new Pgds();
		p.setBarcode(barcode);
		p.setName(pMap.get("产品名称"));
		p.setEname(pMap.get("产品名称(英文)"));
		p.setUnspsc(pMap.get("UNSPSC分类"));
		p.setBrand(pMap.get("品牌名称"));
		p.setSpec(pMap.get("规格型号"));
		p.setOrgiCountry(pMap.get("原产国"));
		p.setProductArea(pMap.get("产地"));
		p.setCodeType(pMap.get("条码类型"));
		p.setComposition(pMap.get("配料"));
		p.setDeep(pMap.get("深度"));
		p.setGreenfoodCode(pMap.get("绿色食品证号"));
		p.setHealthfoodCode(pMap.get("保健食品证号"));
		p.setHealthPermit(pMap.get("卫生许可证号"));
		p.setHeigt(pMap.get("高度"));
		p.setPackMat(pMap.get("包装材料"));
		p.setProductor(pMap.get(""));
		p.setProductPermit(pMap.get("生产许可证号"));
		p.setProStandard(pMap.get("产品执行标准代号"));
		p.setpType(pMap.get("产品所处层级"));
		p.setQsType(pMap.get("QS分类"));
		p.setValidDate(pMap.get("保质期(天)"));
		p.setWidth(pMap.get("宽度"));
		
		logger.info("商品："+JacksonUtils.toJson(p));
		
		return p;
	}
	
	public final static void main(String[] args) throws Exception
	{
		Document doc = null;
        Connection conn = Jsoup.connect("http://www.anccnet.com/goods.aspx?base_id=F25F56A9F703ED74A20FB650AA3F34DC51D6CAADB6F5AE9CCA3549FDA3DB9D4C0588AA1B9FC8A614");
        conn.timeout(60000);
        conn.header("Host", "search.anccnet.com");
        conn.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.header("Accept-Encoding", "gzip,deflate,sdch");
        conn.header("Proxy-Connection", "keep-alive");
        conn.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
        conn.referrer("http://search.anccnet.com/searchResult2.aspx");

        doc = conn.get();
        
        System.out.println(doc.html());
        
//        GdsProductAnalyzer._gdsAnalyzeByHtml("06943150500010", doc.html());
        
	}
}
