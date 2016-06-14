package com.github.xbeowulf.microservices.url_service;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.github.xbeowulf.microservices.sqs.SimpleDispatcher;
import com.github.xbeowulf.microservices.sqs.SimpleListener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UrlServiceMain {

    public static void main(String[] args) {
        SpringApplication.run(UrlServiceMain.class, args);
    }

    @Bean
    public UrlService urlService(){
        return new UrlService();
    }

    @Bean
    public SimpleDispatcher simpleDispatcher(AmazonSQS sqs) {
        return new SimpleDispatcher(sqs, "http://localhost:9325/queue/url-from-twitter");
    }
    @Bean
    public SimpleListener simpleListener(AmazonSQS sqs, UrlService urlService) {
        SimpleListener listener = new SimpleListener(sqs, "http://localhost:9325/queue/url-from-twitter", urlService::onMessage);
        listener.start();

        return listener;
    }

    @Bean
    public AmazonSQS amazonSqs() {
        return new AmazonSQSClient(new BasicAWSCredentials("x", "x"));
    }

}
