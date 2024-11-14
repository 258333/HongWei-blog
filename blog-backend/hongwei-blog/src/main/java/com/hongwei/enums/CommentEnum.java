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
public enum CommentEnum {

    COMMENT_TYPE_ARTICLE(1, "评论类型(1,文章)"),
    COMMENT_TYPE_LEAVE_WORD(2, "评论类型(2,留言板)");

    // 评论类型
    private final Integer type;
    // 描述
    private final String desc;
}
