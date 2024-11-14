package com.hongwei.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: HongWei
 * @date: 2024/11/14 13:02
 * 黑名单类型枚举
 **/
@Getter
@AllArgsConstructor
public enum BlackListEnum {

    // 是否封禁
    IS_BANNED(0, "封禁"),
    IS_NOT_BANNED(1, "未封禁");

    private final Integer code;
    private final String desc;
}
