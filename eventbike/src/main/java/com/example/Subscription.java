package com.example;

/**
 * Created by Yusuzhan on 2017/4/24.
 */

public class Subscription {

    final Object subscriber;
    final SubscriberMethod subscriberMethod;
    volatile boolean active;

    public Subscription(Object subscriber, SubscriberMethod subscriberMethod) {
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
        this.active = true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Subscription) {
            Subscription otherSubscription = (Subscription) other;
            return subscriber == otherSubscription.subscriber
                    && subscriberMethod.equals(otherSubscription.subscriberMethod);
        } else {
            return false;
        }
    }
}
