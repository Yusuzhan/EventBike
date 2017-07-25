package com.example;

import java.lang.reflect.Method;

/**
 * Created by Yusuzhan on 2017/4/24.
 */

public class SubscriberMethod {
    Method method;
    Class<?> eventType;
    ThreadMode threadMode;
    boolean sticky;

    public SubscriberMethod(Method method, Class<?> eventType, ThreadMode threadMode, boolean sticky) {
        this.method = method;
        this.eventType = eventType;
        this.threadMode = threadMode;
        this.sticky = sticky;
    }
}
