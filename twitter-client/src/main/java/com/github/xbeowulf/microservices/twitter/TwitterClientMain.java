package com.github.xbeowulf.microservices.twitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

@SpringBootApplication
public class TwitterClientMain {

    public static void main(String[] args) {
        SpringApplication.run(TwitterClientMain.class, args);
    }

    @Bean
    public static TwitterStream twitterStream() {
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

        return twitterStream;
    }

}
