package com.hongwei.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description:
 * @author: HongWei
 * @date: 2024/11/14 13:10
 * @param: 
 * @return: 
 **/
@Getter
@AllArgsConstructor
public enum LikeEnum {

    LIKE_TYPE_ARTICLE(1, "点赞：文章"),
    LIKE_TYPE_COMMENT(2, "点赞：评论"),
    LIKE_TYPE_LEAVE_WORD(3, "点赞：留言");

    // 类型
    private final Integer type;
    // 描述
    private final String desc;
}
