package com.hongwei.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: HongWei
 * @date: 2024/11/14 13:09
 * @description: 注册类型枚举
 **/
@Getter
@AllArgsConstructor
public enum RegisterOrLoginTypeEnum {

    /**
     * 邮箱或用户名登录
     */
    EMAIL(0, "邮箱登录", "email"),
    /**
     * Gitee
     */
    GITEE(1, "Gitee登录", "gitee"),
    /**
     * Github
     */
    GITHUB(2, "Github登录", "github");


    /**
     * 注册方式
     */
    private final Integer registerType;

    /**
     * 描述
     */
    private final String description;

    /**
     * 策略
     */
    private final String strategy;

}
