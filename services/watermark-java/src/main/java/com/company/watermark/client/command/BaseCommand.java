package com.company.watermark.client.command;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for hystrix support.
 * Hystrix has some nice features like async, circuit breaker and timeout out of the box.
 * Any call either over http or just to slow DB over jdbc can be wrapped in hystrix command.
 */

@Slf4j
public abstract class BaseCommand<R> extends HystrixObservableCommand<R> {

    private final String debugMessage;

    protected BaseCommand(String groupKey, int timeout, String debugMessage) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(timeout))
        );
        this.debugMessage = debugMessage;
    }

    protected void handleErrors() {
        final String message;
        if (isFailedExecution()) {
            message = getMessagePrefix() + "FAILED: " + getFailedExecutionException().getMessage();
        } else if (isResponseTimedOut()) {
            message = getMessagePrefix() + "TIMED OUT";
        } else {
            message = getMessagePrefix() + "SOME OTHER FAILURE";
        }
        log.warn(message);
    }

    private String getMessagePrefix() {
        return this.getClass().getSimpleName() + " [" + debugMessage + "]: ";
    }
}
