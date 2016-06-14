package com.github.xbeowulf.microservices.sqs;

import com.amazonaws.services.sqs.AmazonSQS;

public class SimpleDispatcher {

    private AmazonSQS sqs;
    private String queueName;

    public SimpleDispatcher(AmazonSQS sqs, String queueName) {
        this.sqs = sqs;
        this.queueName = queueName;
    }

    public void sendMessage(String body) {
        sqs.sendMessage(queueName, body);
    }

}
