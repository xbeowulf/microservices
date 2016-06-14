package com.github.xbeowulf.microservices.dev.sqs;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;

import org.elasticmq.rest.sqs.SQSRestServer;
import org.elasticmq.rest.sqs.SQSRestServerBuilder;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

import static org.slf4j.LoggerFactory.getLogger;

public class SQSRestServerStarterMain {

    private static final Logger log = getLogger(SQSRestServerStarterMain.class);

    private static final AtomicReference<SQSRestServer> server = new AtomicReference<>();

    public static void main(String... args) throws Exception {
        start();
        init();
    }

    private static void start() {
        try {
            log.info("Starting SQS server...");
            server.set(SQSRestServerBuilder.withPort(9325).withInterface("localhost").start());

            //getRuntime().addShutdownHook(new Thread(SQSRestServerStarterMain::stop));

        } catch (Exception e) {
            log.error("Failed to start SQS server.", e);
        }
    }

    private static void stop() {
        log.info("Stopping SQS server.");
        server.get().stopAndWait();

        log.info("SQS server stopped.");
    }

    private static void init() {
        log.info("Creating queues...");

        AmazonSQSClient sqs = new AmazonSQSClient(new BasicAWSCredentials("x", "x"));
        sqs.setEndpoint("http://localhost:9325");
        String queueUrl = sqs.createQueue("url-from-twitter").getQueueUrl();

        log.info("Created queue with url {}.", queueUrl);
        sqs.shutdown();

    }
}
