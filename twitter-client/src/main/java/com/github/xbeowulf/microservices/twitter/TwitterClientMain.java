package com.github.xbeowulf.microservices.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class TwitterClientMain {

    private static Logger log = LoggerFactory.getLogger(TwitterClientMain.class);

    public static void main(String[] args) {

        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(new TwitterListener());

        String[] track = {
                "abcnews go com",
                "bloomberg com news",
                "news bbc co uk",
                "cbc ca news",
                "cbsnews com",
                "cnn com",
                "guardian co uk",
                "foxnews com",
                "nytimes com",
                "nbcnews com",
                "thetimes co uk",
                "usatoday com",
                "washingtonpost com",
                "reuters com"};

        twitterStream.filter(new FilterQuery(track));
    }

}
