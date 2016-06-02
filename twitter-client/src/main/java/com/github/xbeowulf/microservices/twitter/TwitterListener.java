package com.github.xbeowulf.microservices.twitter;

import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.StatusAdapter;

import java.util.stream.Stream;

public class TwitterListener extends StatusAdapter {

    private static final Logger log = LoggerFactory.getLogger(TwitterListener.class);

    private static final int DEFAULT_CACHE_SIZE = 100_000;

    private LRUMap<String, String> cache = new LRUMap<>(DEFAULT_CACHE_SIZE);

    @Override
    public void onStatus(Status status) {
        Stream.of(status.getURLEntities())
                .forEach(url -> {

                    String unshortenedUrl = cache.get(url.getExpandedURL());
                    if (unshortenedUrl != null) {
                        log.info("Url from cache: {}.", unshortenedUrl);
                    } else {
                        unshortenedUrl = UrlUtils.getSourceUrl(url.getExpandedURL());
                        cache.put(url.getExpandedURL(), unshortenedUrl);
                        log.info("Got new url: {}.", unshortenedUrl);
                    }

                });
    }

}
