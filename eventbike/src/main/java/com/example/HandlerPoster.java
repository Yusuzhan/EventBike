package com.example;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by Yusuzhan on 2017/4/25.
 */

public class HandlerPoster extends Handler {
    private final PendingPostQueue queue;
    private final EventBike bike;


    public HandlerPoster(Looper looper, EventBike bike) {
        super(looper);
        this.bike = bike;
        queue = new PendingPostQueue();
    }

    void enqueue(Subscription subscription, Object event) {
//        PendingPost pendingPost = new PendingPost(event, subscription);
        PendingPost pendingPost = PendingPost.getInstance(event, subscription);
        synchronized (this) {
            queue.enqueue(pendingPost);
            sendMessage(obtainMessage());
        }
    }

    @Override
    public void handleMessage(Message msg) {
        PendingPost pendingPost = queue.poll();
        bike.invokeSubscriber(pendingPost.subscription, pendingPost.event);
    }
}
