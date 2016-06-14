package com.github.xbeowulf.microservices.url_service;

import org.apache.commons.collections4.map.LRUMap;
import org.jsoup.Connection.Response;
import org.slf4j.Logger;

import static org.jsoup.Jsoup.connect;
import static org.slf4j.LoggerFactory.getLogger;

public class UrlService {

    private static final Logger log = getLogger(UrlService.class);

    private static final int DEFAULT_TIMEOUT_MILLIS = 2000;
    private static final int DEFAULT_CACHE_SIZE = 1000;

    private LRUMap<String, String> cache = new LRUMap<>(DEFAULT_CACHE_SIZE);

    public void onMessage(String body) {

        String url = cache.get(body);
        if (url != null) {
            log.info("Found in cache {} (cache size {}).", url, cache.size());
        } else {
            url = getSourceUrl(body);
            cache.put(body, url);
        }

        log.info(body);
    }

    private String getSourceUrl(String url) {
        String sourceUrl = "";
        try {
            Response response = connect(url)
                    .followRedirects(true)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36")
                    .referrer("http://www.google.com")
                    .timeout(DEFAULT_TIMEOUT_MILLIS)
                    .execute();
            sourceUrl = response.url().toString();
        } catch (Exception e) {
            log.error("Failed to get url {}.", url, e);
        }
        return sourceUrl;
    }
}
