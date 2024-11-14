package com.hongwei.annotation;

import java.lang.annotation.*;

/**
 * Redis限流注解
 *
 */
@Documented  // 表示该注解会出现在Javadoc中
@Target(ElementType.METHOD)  // 表示该注解应用于类级别
@Retention(RetentionPolicy.RUNTIME)  // 表示该注解在运行时仍然可用
public @interface AccessLimit {

    /**
     * 限制周期(秒)
     */
    int seconds();

    /**
     * 规定周期内限制次数
     */
    int maxCount();

    /**
     * 触发限制时的消息提示
     */
    String msg() default "操作过于频繁，请稍后再试";

}
