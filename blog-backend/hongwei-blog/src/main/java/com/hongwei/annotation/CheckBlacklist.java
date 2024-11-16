package com.hongwei.annotation;

import java.lang.annotation.*;

/**
 * @author: HongWei
 * @date: 2024/11/14 12:49
 **/
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckBlacklist {
}
