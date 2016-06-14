package com.github.xbeowulf.microservices.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public class SimpleListener {

    private static final Logger log = getLogger(SimpleListener.class);

    private final AmazonSQS sqs;
    private final String queueName;
    private final Consumer<String> action;

    private final ExecutorService executor;

    public SimpleListener(AmazonSQS sqs, String queueName, Consumer<String> action) {
        this.sqs = sqs;
        this.queueName = queueName;
        this.action = action;

        executor = newSingleThreadExecutor();
    }

    public void start() {
        executor.submit(() -> {

            while (!executor.isShutdown()) {
                List<Message> messages = sqs.receiveMessage(queueName).getMessages();
                for (Message message : messages) {
                    if (!executor.isShutdown()) {
                        try {
                            action.accept(message.getBody());
                        } catch (Exception e) {
                            log.error("Failed to process message: {}.", message);
                        }
                        sqs.deleteMessage(queueName, message.getReceiptHandle());
                    }
                }
            }
        });
    }

    public void stop() {
        try {
            executor.shutdown();
            executor.awaitTermination(2000, MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting shutdown.");
        } finally {
            executor.shutdownNow();
            log.info("Shutdown finished.");
        }
    }

}
