package com.example;

import android.os.Looper;

import com.example.util.L;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yusuzhan on 2017/4/23.
 */

public class EventBike {

    private static EventBike defaultBike;
    private static final EventBikeBuilder DEFAULT_BUILDER = new EventBikeBuilder();

    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    private final Map<Object, List<Class<?>>> typesBySubscriber;

    private SubscriberMethodsFinder subscriberMethodsFinder = new SubscriberMethodsFinder();

    private final HandlerPoster handlerPoster;
    private final BackgroundPoster backgroundPoster;
    private final AsyncPoster asyncPoster;

    final ExecutorService executorService;
    /**
     * provide independent PostingStates for main thread and background threads
     */
    private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };

    public EventBike(EventBikeBuilder builder) {
        subscriptionsByEventType = new HashMap<>();
        typesBySubscriber = new HashMap<>();

        handlerPoster = new HandlerPoster(Looper.getMainLooper(), this);
        backgroundPoster = new BackgroundPoster(this);
        executorService = Executors.newCachedThreadPool();//TODO executorService
        asyncPoster = new AsyncPoster(this);
    }

    public static EventBike getBike() {
        if (defaultBike == null) {
            defaultBike = new EventBike(DEFAULT_BUILDER);
        }
        return defaultBike;
    }

    public void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
        List<SubscriberMethod> subscriberMethods = subscriberMethodsFinder.findSubscriberMethods(subscriberClass);
        synchronized (this) {
            for (SubscriberMethod method : subscriberMethods) {
                subscribe(subscriber, method);
            }
        }
    }


    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        Class<?> eventType = subscriberMethod.eventType;
        L.i("subscriber event type: " + eventType.getName());
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod); //make a new subscription
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions == null) {
            subscriptions = new CopyOnWriteArrayList<>();
            subscriptionsByEventType.put(eventType, subscriptions);
        }
        subscriptions.add(newSubscription);

        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<>();
            typesBySubscriber.put(subscriber, subscribedEvents);
        }
        subscribedEvents.add(eventType);
        L.i("subscribe " + System.nanoTime());
        //TODO 处理sticky event
    }

    public void unregister(Object subscriber) {
        List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
        if (subscribedTypes == null) {
            L.i("current object is not registered to any EventBike");
            return;
        }

        for (Class<?> eventType : subscribedTypes) {
            unsubscribeByEventType(eventType, subscriber);
        }
        typesBySubscriber.remove(subscriber);
        L.i("unregister " + subscriber.getClass().getName() + " left " + typesBySubscriber.size() + " subscribers");
    }

    /**
     * Only updates subscriptionsByEventType, not typesBySubscriber! Caller must update typesBySubscriber.
     */
    private void unsubscribeByEventType(Class<?> eventType, Object subscriber) {
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        int size = subscriptions.size();
        for (int i = 0; i < size; i++) {
            Subscription subscription = subscriptions.get(i);
            if (subscriber == subscription.subscriber) {
                subscription.active = false;
                subscriptions.remove(i);
                i--;
                size--;
            }
        }
        L.i("unregister " + subscriber.getClass().getSimpleName() + " from " + eventType.getSimpleName() + " event.");
    }

    public void post(Object event) {
        PostingThreadState postingState = currentPostingThreadState.get();
        List<Object> eventQueue = postingState.eventQueue;
        eventQueue.add(event);
        L.v("current thread event queue size: " + eventQueue.size());

        postingState.isMainThread = Looper.getMainLooper() == Looper.myLooper();
        while (!eventQueue.isEmpty()) {
            postSingleEvent(eventQueue.remove(0), postingState);
        }
    }

    private void postSingleEvent(Object event, PostingThreadState postingState) {
        Class<?> eventType = event.getClass();

        CopyOnWriteArrayList<Subscription> subscriptions;
        synchronized (this) {
            subscriptions = subscriptionsByEventType.get(eventType);
        }
        if (subscriptions == null || subscriptions.isEmpty()) {
            return;
        }

        for (Subscription subscription : subscriptions) {
            postingState.event = event;
            postingState.subscription = subscription;
            postToSubscription(subscription, event, postingState.isMainThread);
        }
    }

    private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
        switch (subscription.subscriberMethod.threadMode) {
            case POSTING:
                // main 2 main
                // back 2 back
                invokeSubscriber(subscription, event);
                break;
            case MAIN:
                if (isMainThread) {
                    //main 2 main
                    invokeSubscriber(subscription, event);
                } else {
                    // back 2 main
                    handlerPoster.enqueue(subscription, event);
                }
                break;
            case BACKGROUND:
                // main 2 background
                if (isMainThread) {
                    L.i("main 2 background");
                    backgroundPoster.enqueue(subscription, event);
                } else {
                    //back 2 back
                    L.i("back 2 back");
                    invokeSubscriber(subscription, event);
                }
                break;
            case ASYNC:
                asyncPoster.enqueue(subscription, event);
                break;
        }
    }

    public void invokeSubscriber(PendingPost pendingPost) {
        Subscription subscription = pendingPost.subscription;
        Object event = pendingPost.event;
        PendingPost.releasePendingPost(pendingPost);
        if (subscription.active) {
            invokeSubscriber(subscription, event);
        }
    }

    public void invokeSubscriber(Subscription subscription, Object event) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    static class PostingThreadState {
        final List<Object> eventQueue = new ArrayList<>();
        boolean isMainThread;
        Subscription subscription;
        Object event;
    }

}
