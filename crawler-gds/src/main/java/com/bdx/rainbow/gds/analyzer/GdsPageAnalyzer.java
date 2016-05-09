package com.bdx.rainbow.gds.analyzer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bdx.rainbow.common.util.HttpClientUtil;
import com.bdx.rainbow.crawler.analyzer.Analyzer;
import com.bdx.rainbow.crawler.bean.BeanFactory;
import com.bdx.rainbow.crawler.seed.Seed;
import com.bdx.rainbow.gds.configuration.CrawlerConfig;
import com.bdx.rainbow.gds.entity.KeywordGds;
import com.bdx.rainbow.gds.mapper.KeywordGdsMapper;
import com.bdx.rainbow.gds.seed.GdsListPageSeed;
import com.bdx.rainbow.gds.seed.GdsPageSeed;

@Component
public class GdsPageAnalyzer implements Analyzer {

	private Logger logger = LoggerFactory.getLogger(GdsPageAnalyzer.class);
	
	private BeanFactory beanFactory = BeanFactory.instance();
	
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

        String url = ((GdsPageSeed) seed).getUrl();
        
        logger.debug("请求地址："+url);
        
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
		
//		logger.debug(html);
		
		Document doc = Jsoup.parse(html,"utf-8");
		
		/** 第一页已经分析，从第二页开始取 **/
		Elements page_info_elements = doc.select("div#myPager > table:eq(0) > tbody:eq(0) > tr:eq(0) > td:eq(0) > font");
		String count = page_info_elements.get(0).text();//总记录
		String countPage = page_info_elements.get(1).text();//总页数
		
		Integer page = Integer.valueOf(countPage);
		
		Map<Class<? extends Seed>,Collection<Seed>> seedMap = new HashMap<Class<? extends Seed>, Collection<Seed>>();
		seedMap.put(GdsListPageSeed.class, new HashSet<Seed>());
		for(int i=1;i<=page;i++)
		{
			GdsListPageSeed _seed = new GdsListPageSeed();
			_seed.setKeyword(((GdsPageSeed) seed).getKeyword());
			_seed.setrPage(i);
			seedMap.get(GdsListPageSeed.class).add(_seed);
		}
		
		/** 设置需要抓取的总条数 **/
		KeywordGdsMapper mapper = (KeywordGdsMapper) beanFactory.getBean(KeywordGdsMapper.class);
		KeywordGds keyword = mapper.selectByPrimaryKey(((GdsPageSeed) seed).getKeyword());
		keyword.setTotal(Long.valueOf(count));
		mapper.updateByPrimaryKeySelective(keyword);
        return seedMap;
	}

}
