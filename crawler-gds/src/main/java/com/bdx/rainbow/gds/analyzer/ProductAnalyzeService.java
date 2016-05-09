package com.bdx.rainbow.gds.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import com.gargoylesoftware.htmlunit.BrowserVersion;
//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;

@Service
@Transactional
public class ProductAnalyzeService {
	
	/**
	 * gds网站通过条形码搜索的首页面解析
	 * 解析出商品详细信息和厂家信息的详细页面内容
	 */
	public static void gdsAnalyze(String url) throws Exception
	{
		Document doc = null;
        Connection conn = Jsoup.connect(url);
        conn.timeout(60000);
        conn.header("Host", "search.anccnet.com");
        conn.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.header("Accept-Encoding", "gzip,deflate,sdch");
        conn.header("Proxy-Connection", "keep-alive");
        conn.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36");
        conn.referrer("http://www.gds.org.cn/");

        doc = conn.get();

        Elements dls = doc.getElementsByTag("dl");

        for (Element dl : dls) {
            
            Elements dts = dl.getElementsByTag("dt");
            Elements dds = dl.getElementsByTag("dd");

            for (int i = 0; i < dts.size(); i++) {
                if (dts.get(i).text().contains("商品条码") || dts.get(i).text().contains("发布厂家")) {
                    String gdsUrl = dds.get(i).select("a").attr("href");
                    
                    System.out.println(gdsUrl);
//                    resultMap.put(gdsUrl, Jsoup.connect(gdsUrl).timeout(timeout).post().html());
                }
            }
        }
	}
	
//	private Pgds findByHtmlUnit(String barcode,String url) throws Exception
//	{
//		if(StringUtil.isBlank(url))
//			throw new Exception("URL 为空");
//		/** HtmlUnit请求web页面 */  
//	    WebClient wc = new WebClient(BrowserVersion.CHROME);  
//	    wc.getOptions().setUseInsecureSSL(true);  
//	    wc.getOptions().setJavaScriptEnabled(true); // 启用JS解释器，默认为true  
//	    wc.getOptions().setCssEnabled(false); // 禁用css支持  
//	    wc.getOptions().setThrowExceptionOnScriptError(false); // js运行错误时，是否抛出异常  
//	    wc.getOptions().setTimeout(100000); // 设置连接超时时间 ，这里是10S。如果为0，则无限期等待  
//	    wc.getOptions().setDoNotTrackEnabled(false);
////	    ProxyConfig proxyConfig = new ProxyConfig();
////	    proxyConfig.setProxyHost("10.10.19.78");
////	    proxyConfig.setProxyPort(8080);
////	    proxyConfig.setSocksProxy(false);
////	    wc.getOptions().setProxyConfig(proxyConfig);
//	    HtmlPage page = wc.getPage(url);  
//	  
//	    String html = page.asXml();
//	    
//		return gdsAnalyzeByHtml(barcode, html);
//	}
	
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
	}
}
