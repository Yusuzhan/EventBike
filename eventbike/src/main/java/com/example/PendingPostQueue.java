package com.example;

/**
 * Created by Yusuzhan on 2017/4/26.
 */

public class PendingPostQueue {

    private PendingPost head;
    private PendingPost tail;

    synchronized void enqueue(PendingPost pendingPost) {
        if (tail != null) {
            tail.next = pendingPost;
            tail = pendingPost;
        } else if (head == null) {
            head = tail = pendingPost;
        }
        notifyAll();//notify threads willing to call this method
    }

    synchronized PendingPost poll() {
        PendingPost poll = head;
        if (head != null) {
            head = head.next;
            if (head == null) {
                tail = null;
            }
        }
        return poll;
    }

}
