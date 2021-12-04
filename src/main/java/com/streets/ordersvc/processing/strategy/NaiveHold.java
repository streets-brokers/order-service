package com.streets.ordersvc.processing.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NaiveHold implements Strategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(NaiveHold.class);

    @Override
    public String name() {
        return null;
    }

    @Override
    public void execute() {

    }
}
