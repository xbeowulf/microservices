package com.github.xbeowulf.microservices.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.slf4j.LoggerFactory.getLogger;

public class SimpleSqsListener {

    private static final Logger log = getLogger(SimpleSqsListener.class);

    private final AmazonSQS sqs;
    private final Consumer<String> action;
    private final String queueUrl;

    private Integer maxNumberOfMessages = 10;
    private Integer backOffTimeSeconds = 5;
    private Integer visibilityTimeoutSeconds;
    private Integer waitTimeSeconds;

    private AtomicBoolean isRunning;

    public SimpleSqsListener(AmazonSQS sqs, String queueUrl, Consumer<String> action) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.action = action;
        this.isRunning = new AtomicBoolean(false);
    }

    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            doStart();
        }
    }

    private void doStart() {
        newSingleThreadExecutor().submit(() -> {

            ReceiveMessageRequest receiveMessageRequest = buildReceiveMessageRequest();
            ExecutorService messageActionExecutor = newFixedThreadPool(receiveMessageRequest.getMaxNumberOfMessages());
            while (isRunning.get()) {

                try {
                    List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
                    CountDownLatch messageBatchLatch = new CountDownLatch(messages.size());

                    for (Message message : messages) {
                        if (isRunning.get()) {

                            messageActionExecutor.submit(() -> {
                                try {
                                    action.accept(message.getBody());
                                } catch (Exception e) {
                                    log.error("Failed to process message: {}.", message);
                                } finally {
                                    sqs.deleteMessage(getQueueUrl(), message.getReceiptHandle());
                                    messageBatchLatch.countDown();
                                }
                            });

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
                    log.warn("Failed to poll queue {}. Will retry in {} seconds.",
                            getQueueUrl(), getBackOffTimeSeconds(), e);
                    try {
                        TimeUnit.SECONDS.sleep(getBackOffTimeSeconds());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    public void stop() {
        isRunning.set(false);
    }

    private ReceiveMessageRequest buildReceiveMessageRequest() {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(getQueueUrl())
                .withMaxNumberOfMessages(getMaxNumberOfMessages());

        if (getVisibilityTimeoutSeconds() != null) {
            receiveMessageRequest.withVisibilityTimeout(getVisibilityTimeoutSeconds());
        }

        if (getWaitTimeSeconds() != null) {
            receiveMessageRequest.withWaitTimeSeconds(getWaitTimeSeconds());
        }

        return receiveMessageRequest;
    }

    private String getQueueUrl() {
        return queueUrl;
    }

    public SimpleSqsListener withMaxNumberOfMessages(int maxNumberOfMessages) {
        this.maxNumberOfMessages = maxNumberOfMessages;
        return this;
    }

    private int getMaxNumberOfMessages() {
        return maxNumberOfMessages;
    }

    public SimpleSqsListener withVisibilityTimeout(Integer visibilityTimeoutSeconds) {
        this.visibilityTimeoutSeconds = visibilityTimeoutSeconds;
        return this;
    }

    private Integer getVisibilityTimeoutSeconds() {
        return visibilityTimeoutSeconds;
    }

    public SimpleSqsListener withWaitTime(Integer waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
        return this;
    }

    private Integer getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    public SimpleSqsListener withBackOffTime(int backOffTimeSeconds) {
        this.backOffTimeSeconds = backOffTimeSeconds;
        return this;
    }

    private long getBackOffTimeSeconds() {
        return backOffTimeSeconds;
    }
}
