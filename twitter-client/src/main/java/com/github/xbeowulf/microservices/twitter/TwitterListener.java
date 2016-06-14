package com.github.xbeowulf.microservices.twitter;

import com.github.xbeowulf.microservices.sqs.SimpleDispatcher;

import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import twitter4j.Status;
import twitter4j.StatusAdapter;

import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class TwitterListener extends StatusAdapter {

    private static final Logger log = getLogger(TwitterListener.class);

    private static final int DEFAULT_CACHE_SIZE = 1000;

    private LRUMap<String, String> cache = new LRUMap<>(DEFAULT_CACHE_SIZE);

    @Autowired
    private SimpleDispatcher simpleDispatcher;


    @Override
    public void onStatus(Status status) {
        Stream.of(status.getURLEntities())
                .forEach(url -> {

                    String expandedUrl = url.getExpandedURL();
                    if (cache.containsKey(expandedUrl)) {
                        log.info("Found in cache {} (cache size {}).", expandedUrl, cache.size());
                    } else {
                        cache.put(expandedUrl, null);
                        simpleDispatcher.sendMessage(expandedUrl);

                        log.info("Sent to queue: {}.", expandedUrl);
                    }
                });
    }


}
