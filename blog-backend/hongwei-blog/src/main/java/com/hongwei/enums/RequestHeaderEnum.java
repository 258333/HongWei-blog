package com.hongwei.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: HongWei
 * @date: 2024/11/14 13:13
 * @param: 
 * @return:  
 * @description:
 **/
@Getter
@AllArgsConstructor
public enum RequestHeaderEnum {

    /**
     * Github获取个人信息Accept请求头
     */
    GITHUB_USER_INFO("Accept", "application/vnd.github.v3+json");


    /**
     * 请求头
     */
    public final String header;

    /**
     * 内容
     */
    public final String content;
}
