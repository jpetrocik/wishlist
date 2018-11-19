package org.psoft.wishlist.service;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class ScraperService {
	private static Log logger = LogFactory.getLog(ScraperService.class);

	CloseableHttpClient httpClient;

	private Timer abortRequestTimer = new Timer(true);

	@PostConstruct
	public void init() {
		
		PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
		poolingHttpClientConnectionManager.setMaxTotal(100);
		poolingHttpClientConnectionManager.setDefaultMaxPerRoute(10);

//		RequestConfig requestConfig = RequestConfig.custom()
//				.setSocketTimeout(15000)
//				.setConnectTimeout(15000)
//				.setCircularRedirectsAllowed(true)
//				.build();


			HttpClientBuilder httpClientBuilder = HttpClients.custom()
				.setConnectionManager(poolingHttpClientConnectionManager)
				.setUserAgent("Mozilla/5.0 Gift Registry Gifter");
//				.setDefaultRequestConfig(requestConfig);
			
			httpClient = httpClientBuilder.build();
	}
	
	Map<String, String> downloadPage(URL pageUrl) {

		Map<String, String> metadata = new HashMap<>();

		CloseableHttpResponse closeableHttpResponse = null;

		try {
			// Request
			HttpGet httpGet = new HttpGet(pageUrl.toURI());
			httpGet.setHeader("accept-language","en-US,en;q=0.9");
			httpGet.setHeader("Content-Type","text/html");
			HttpClientContext httpClientContext = new HttpClientContext();

			//		if (useCookies) {
			//			// Cookies
			//			BasicCookieStore basicCookieStore = new BasicCookieStore();
			//
			//			// Domain/Cookie Lookup
			//			String domain = url.getHost();
			//			logger.debug("Looking up cookies for: " + domain);
			//			List<Cookies> cookies = cookiesService.getCookiesByDomain(domain);
			//			if (cookies != null) {
			//				logger.debug("Cookies found for: " + domain);
			//				for (Cookies cookie : cookies) {
			//					BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
			//					clientCookie.setDomain(cookie.getDomain());
			//					clientCookie.setPath(cookie.getPath());
			//					logger.debug("Adding Cookie: " + clientCookie.toString());
			//					basicCookieStore.addCookie(clientCookie);
			//				}
			//			}
			//
			//			httpClientContext.setCookieStore(basicCookieStore);
			//
			//		}

			//Hard abort for anything taking too long
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					if (httpGet != null) {
						httpGet.abort();
					}
				}
			};
			abortRequestTimer.schedule(task, 10000);

			closeableHttpResponse = httpClient.execute(httpGet, httpClientContext);

			//if http request completes before being aborted cancel it
			task.cancel();

			if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
				logger.warn(closeableHttpResponse.getStatusLine().getStatusCode() + " status code downloading page at " + pageUrl);
			}

			HttpEntity entity = closeableHttpResponse.getEntity();
			InputStream inStream = entity.getContent();

			Document doc = getDocument(inStream, pageUrl);
			metaTags(doc, metadata);

			EntityUtils.consume(entity);

		} catch (Exception e){
			logger.error("Failed to fetch " + pageUrl, e);
		} finally {

			try {
				if (closeableHttpResponse != null)
					closeableHttpResponse.close();
			} catch (Exception e){
				logger.error("Failed to close http response, leaking pool connections");
			}
		}
		
		return metadata;
	}
	
	protected void metaTags(Document doc, Map<String, String> metadata) {

		if (doc == null) {
			return;
		}

		try {

			//title
			if (doc.select("meta[property=og:title]").first() != null) {
				metadata.put("title", doc.select("meta[property=og:title]").first().attr("content"));
			} else if (doc.select("title").first() != null) {
				metadata.put("title", doc.title());
			}

			//description
			if (doc.select("meta[property=og:description]").first() != null) {
				metadata.put("description", doc.select("meta[property=og:description]").first().attr("content"));
			} else if (doc.select("meta[name=description]").first() != null) {
				metadata.put("description", doc.select("meta[name=description]").first().attr("content"));
			}

			//og:site name
			if (doc.select("meta[property=og:site_name]").first() != null) {
				metadata.put("site", doc.select("meta[property=og:site_name]").first().attr("content"));
			}

			//og:preview
			String[] imagePreviewTags = new String[] {"meta[property=og:image:secure_url]","meta[property=og:image]","meta[property=og:image:url]"};
			for (String tag : imagePreviewTags) {
				Element imgTag = doc.select(tag).first();
				if (imgTag != null) {
					String value = imgTag.attr("content");
					if (StringUtils.isNotBlank(value)) {
						metadata.put("image", value);
						break;
					}
				}
			}

		} catch (Exception e) {
			logger.error("Failed to extract meta tags", e);
		}
	}
	
	org.jsoup.nodes.Document getDocument(InputStream inStream, URL pageUrl) throws Exception {
		String baseUrl = baseUrl(pageUrl);
		org.jsoup.nodes.Document doc = Jsoup.parse(inStream, "UTF-8", baseUrl);
		return doc;
	}

	String baseUrl(URL pageUrl) {
		String link = pageUrl.toString();
		int lastPath = link.lastIndexOf('/');
		if (lastPath > -1)
			return link.substring(0, lastPath);

		return link;
	}

}
