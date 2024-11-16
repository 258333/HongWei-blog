package com.hongwei.mapper;

import com.hongwei.domain.email.CommentEmail;

/**
 * @author: HongWei
 * @date: 2024/11/16 11:33
 **/
public interface EmailInformMapper {

    /**
     * 查询用户评论信息
     * @param commentId 评论id
     * @param type 评论类型
     * @return 需要的信息
     */
    CommentEmail getCommentEmailOne(String commentId, Integer type);
}
