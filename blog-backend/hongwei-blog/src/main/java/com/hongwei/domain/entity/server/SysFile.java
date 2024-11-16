package com.hongwei.domain.entity.server;

import lombok.Data;

/**
 * @author: HongWei
 * @date: 2024/11/16 11:21
 * @description: 系统文件相关信息
 **/
@Data
public class SysFile
{
    /**
     * 盘符路径
     */
    private String dirName;

    /**
     * 盘符类型
     */
    private String sysTypeName;

    /**
     * 文件类型
     */
    private String typeName;

    /**
     * 总大小
     */
    private String total;

    /**
     * 剩余大小
     */
    private String free;

    /**
     * 已经使用量
     */
    private String used;

    /**
     * 资源的使用率
     */
    private double usage;

}
