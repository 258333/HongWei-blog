package com.hongwei.enums;

import com.hongwei.constants.ImageConst;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author: HongWei
 * @date: 2024/11/14 13:10
 * @param:
 * @return:
 * @description: 文件上传枚举
 **/
@Getter
@AllArgsConstructor
public enum UploadEnum {

    // 站长头像
    WEBSITE_INFO_AVATAR("websiteInfo/avatar/", "站长头像", List.of(ImageConst.JPG, ImageConst.JPEG, ImageConst.PNG, ImageConst.WEBP), 0.3),
    // 站长背景
    WEBSITE_INFO_BACKGROUND("websiteInfo/background/", "站长背景", List.of(ImageConst.JPG, ImageConst.JPEG, ImageConst.PNG, ImageConst.WEBP), 0.3),
    // 文章封面
    ARTICLE_COVER("article/articleCover/", "文章封面", List.of(ImageConst.JPG, ImageConst.JPEG, ImageConst.PNG, ImageConst.WEBP), 0.3),
    // 文章图片
    ARTICLE_IMAGE("article/articleImage/", "文章图片", List.of(ImageConst.JPG, ImageConst.JPEG, ImageConst.PNG, ImageConst.GIF, ImageConst.WEBP), 3.0),
    // 用户头像
    USER_AVATAR("user/avatar/", "用户头像", List.of(ImageConst.JPG, ImageConst.JPEG, ImageConst.PNG, ImageConst.WEBP), 0.3),
    // 前台首页Banners图片
    UI_BANNERS("banners/", "前台首页Banners图片", List.of(ImageConst.JPG, ImageConst.JPEG, ImageConst.PNG, ImageConst.WEBP), 0.3);


    // 上传目录
    private final String dir;

    // 描述
    private final String description;

    // 支持的格式
    private final List<String> format;

    // 文件最大大小 单位：MB
    private final Double limitSize;
}
