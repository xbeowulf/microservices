package com.github.xbeowulf.microservices.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.util.stream.Stream;

import static com.github.xbeowulf.microservices.twitter.UrlUtils.getSourceUrl;

public class TwitterClientMain {

    private static Logger log = LoggerFactory.getLogger(TwitterClientMain.class);

    public static void main(String[] args) {

        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                Stream.of(status.getURLEntities())
                        .forEach(url -> log.info("URL: " + getSourceUrl(url.getExpandedURL())));
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                // do nothing
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                // do nothing
            }

            public void onScrubGeo(long userId, long upToStatusId) {
                // do nothing
            }

            public void onStallWarning(StallWarning warning) {
                // do nothing
            }

            public void onException(Exception ex) {
                // do nothing
            }
        };

        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);

        String[] track = {"news.bbc.co.uk",
                "usatoday.com",
                "www.thetimes.co.uk",
                "nytimes.com",
                "washingtonpost.com",
                "voanews.com",
                "spiegel.de",
                "slate.com",
                "reuters.com",
                "guardian.co.uk",
                "ap.org"};

        twitterStream.filter(new FilterQuery(track));
    }

}
