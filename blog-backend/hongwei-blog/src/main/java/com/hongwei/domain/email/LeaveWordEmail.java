package com.hongwei.domain.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: HongWei
 * @date: 2024/11/16 11:13
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveWordEmail {
    // 留言头像
    private String avatar;
    // 留言用户的昵称
    private String nickname;
    // 留言内容
    private String content;
    // 留言时间
    private String time;
    // 留言的地址
    private String url;
}
