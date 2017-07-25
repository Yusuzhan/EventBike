package com.example;

/**
 * Created by Yusuzhan on 2017/4/26.
 */

public class AsyncPoster implements Runnable{
    private final PendingPostQueue queue;
    private EventBike bike;

    public AsyncPoster(EventBike bike) {
        this.bike = bike;
        queue = new PendingPostQueue();
    }

    public void enqueue(Subscription subscription, Object event){
        PendingPost pendingPost = PendingPost.getInstance(event, subscription);
        queue.enqueue(pendingPost);
        bike.executorService.execute(this);
    }

    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        bike.invokeSubscriber(pendingPost);
    }
}
