package com.hongwei.domain.entity.server;

import lombok.Data;

/**
 * @author: HongWei
 * @date: 2024/11/16 11:21
 * @description: 系统相关信息
 **/
@Data
public class Sys
{
    /**
     * 服务器名称
     */
    private String computerName;

    /**
     * 服务器Ip
     */
    private String computerIp;

    /**
     * 项目路径
     */
    private String userDir;

    /**
     * 操作系统
     */
    private String osName;

    /**
     * 系统架构
     */
    private String osArch;

}
