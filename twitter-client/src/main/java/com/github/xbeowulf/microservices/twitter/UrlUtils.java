package com.github.xbeowulf.microservices.twitter;

import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jsoup.Jsoup.connect;

public final class UrlUtils {

    private static Logger log = LoggerFactory.getLogger(UrlUtils.class);

    private UrlUtils() {
        throw new UnsupportedOperationException("Instance creation does not supported.");
    }

    public static String getSourceUrl(String url) {
        String resolvedUrl = "";
        try {
            Response response = connect(url)
                    .followRedirects(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36")
                    .referrer("http://www.google.com")
                    .timeout(1000)
                    .execute();
            resolvedUrl = response.url().toString();
        } catch (Exception e) {
            log.error("Failed to get url {}.", url, e);
        }
        return resolvedUrl;
    }

}
