package com.example;

import com.example.util.L;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yusuzhan on 2017/4/24.
 */

public class SubscriberMethodsFinder {

    public List<SubscriberMethod> findSubscriberMethods(Class<?> clazz) {
        FindState state = new FindState();
        state.initForSubscriber(clazz);
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
            if (subscribeAnnotation == null) {
                continue;
            }
            L.i("found subscriber method: " + method.getName());

            state.subscriberMethods.add(new SubscriberMethod(method,
                    method.getParameterTypes()[0],
                    subscribeAnnotation.threadMode(),
                    subscribeAnnotation.sticky()));
        }
        return state.subscriberMethods;
    }

    static class FindState {
        final List<SubscriberMethod> subscriberMethods = new ArrayList<>();

        Class<?> subscriberClass;

        void initForSubscriber(Class<?> clazz) {
            this.subscriberClass = clazz;
        }
    }

}
