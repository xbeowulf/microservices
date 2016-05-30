package com.github.xbeowulf.microservices.twitter;

import org.jsoup.Connection.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.jsoup.Jsoup.connect;

public final class UrlUtils {

    private static Logger log = LoggerFactory.getLogger(UrlUtils.class);

    private UrlUtils() {
        throw new UnsupportedOperationException("Instance creation does not supported.");
    }

    public static String getSourceUrl(String url) {
        String resolvedUrl = "";
        try {
            Response response = connect(url).followRedirects(true).execute();
            resolvedUrl = response.url().toString();
        } catch (IOException e) {
            log.error("Failed to get url {}: {}.", url, e);
        }
        return resolvedUrl;
    }

}
