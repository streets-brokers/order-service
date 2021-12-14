package com.streets.ordersvc.queue;

public interface MessagePublisher {

    void publish(final String message);
}