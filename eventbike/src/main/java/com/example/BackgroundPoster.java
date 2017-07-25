package com.example;

/**
 * Created by Yusuzhan on 2017/4/26.
 */

/**
 * This poster uses a single background thread that deliver will all its events
 * sequentially.
 * So handlers should return quickly to avoid blocking this thread;
 */
public class BackgroundPoster implements Runnable {

    private final PendingPostQueue queue;
    private final EventBike bike;

    private volatile boolean excutorRunning;

    public BackgroundPoster(EventBike bike) {
        this.queue = new PendingPostQueue();
        this.bike = bike;
    }

    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.getInstance(event, subscription);
        synchronized (this) {
            queue.enqueue(pendingPost);
            if (!excutorRunning) {
                excutorRunning = true;
                bike.executorService.execute(this);
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                PendingPost pendingPost = queue.poll();
                if (pendingPost == null) {
                    synchronized (this) {
                        pendingPost = queue.poll();
                        if (pendingPost == null) {
                            excutorRunning = false;
                            return;
                        }
                    }
                }
                bike.invokeSubscriber(pendingPost);
            }
        } finally {
            excutorRunning = false;
        }

    }
}
