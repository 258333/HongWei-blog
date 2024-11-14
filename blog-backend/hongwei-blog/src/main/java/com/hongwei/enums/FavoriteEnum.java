package com.hongwei.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:
 * @author: HongWei
 * @date: 2024/11/14 13:05
 * @param: 
 * @return: 
 **/
@Getter
@AllArgsConstructor
public enum FavoriteEnum {

    FAVORITE_TYPE_ARTICLE(1, "收藏：文章"),
    FAVORITE_TYPE_LEAVE_WORD(2, "收藏：留言");
    // 类型
    private final Integer type;
    // 描述
    private final String desc;
}
