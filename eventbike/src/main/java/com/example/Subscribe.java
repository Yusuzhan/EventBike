package com.example;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Yusuzhan on 2017/4/24.
 */

// annotation exists in bytecode, can be accessed during runtime
@Retention(RetentionPolicy.RUNTIME)
// annotation works for methods
@Target({ElementType.METHOD})
public @interface Subscribe {
    ThreadMode threadMode() default ThreadMode.POSTING;

    boolean sticky() default false;
}
