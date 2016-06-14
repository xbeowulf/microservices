package com.github.xbeowulf.microservices.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.slf4j.LoggerFactory.getLogger;

public class SimpleSqsListener {

    private static final Logger log = getLogger(SimpleSqsListener.class);

    private static final int DEFAULT_MAX_NUMBER_OF_MESSAGES = 5;

    private final AmazonSQS sqs;
    private final Consumer<String> action;

    private final ReceiveMessageRequest receiveMessageRequest;
    private long backOffTimeSeconds = 5;
    private volatile boolean isRunning;

    public SimpleSqsListener(AmazonSQS sqs, String queueUrl, Consumer<String> action) {
        this.sqs = sqs;
        this.action = action;

        receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
                .withMaxNumberOfMessages(DEFAULT_MAX_NUMBER_OF_MESSAGES);
    }

    public void start() {
        isRunning = true;
        newSingleThreadExecutor().submit(() -> {

            ExecutorService messageActionExecutor = newFixedThreadPool(getMaxNumberOfMessages());
            while (isRunning) {

                try {
                    List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
                    log.info("Received {} messages.", messages.size());
                    CountDownLatch messageBatchLatch = new CountDownLatch(messages.size());

                    for (Message message : messages) {
                        if (isRunning) {

                            messageActionExecutor.submit(() -> {
                                try {
                                    action.accept(message.getBody());
                                } catch (Exception e) {
                                    log.error("Failed to process message: {}.", message);
                                } finally {
                                    messageBatchLatch.countDown();
                                }
                            });

                            sqs.deleteMessage(getQueueUrl(), message.getReceiptHandle());
                        } else {
                            messageBatchLatch.countDown();
                        }
                    }

                    try {
                        messageBatchLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                } catch (Exception e) {
                    log.warn("Failed to poll queue {}. Will retry in {} seconds.", getQueueUrl(), getBackOffTimeSeconds(), e);
                    try {
                        Thread.sleep(getBackOffTimeSeconds() * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    public void stop() {
        isRunning = false;
    }

    private String getQueueUrl() {
        return receiveMessageRequest.getQueueUrl();
    }

    public SimpleSqsListener withMaxNumberOfMessages(int maxNumberOfMessages) {
        receiveMessageRequest.withMaxNumberOfMessages(maxNumberOfMessages);
        return this;
    }

    private Integer getMaxNumberOfMessages() {
        return receiveMessageRequest.getMaxNumberOfMessages();
    }

    public SimpleSqsListener withVisibilityTimeoutSeconds(int visibilityTimeout) {
        receiveMessageRequest.withVisibilityTimeout(visibilityTimeout);
        return this;
    }

    public SimpleSqsListener withWaitTimeSeconds(int waitTime) {
        receiveMessageRequest.withWaitTimeSeconds(waitTime);
        return this;
    }

    public SimpleSqsListener withBackOffTimeSeconds(long backOffTimeSeconds) {
        this.backOffTimeSeconds = backOffTimeSeconds;
        return this;
    }

    private long getBackOffTimeSeconds() {
        return backOffTimeSeconds;
    }

}
