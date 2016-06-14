package com.github.xbeowulf.microservices.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.slf4j.LoggerFactory.getLogger;

public class SimpleListener {

    private static final Logger log = getLogger(SimpleListener.class);

    private AmazonSQS sqs;
    private String queueName;
    private Consumer<String> action;

    private boolean isRunning;

    private ExecutorService executor = newSingleThreadExecutor();

    public SimpleListener(AmazonSQS sqs, String queueName, Consumer<String> action) {
        this.sqs = sqs;
        this.queueName = queueName;
        this.action = action;
    }

    public void start(){
        isRunning = true;
        executor.submit( () -> {

            while (isRunning) {
                List<Message> messages = sqs.receiveMessage(queueName).getMessages();
                for (Message message : messages) {
                    try {
                        action.accept(message.getBody());
                        sqs.deleteMessage(queueName, message.getReceiptHandle());
                    } catch (Exception e){
                        log.error("Failed to process message: {}.", message);
                    }

                }
            }
        });
    }

    public void stop() {
        isRunning = false;
    }

}
