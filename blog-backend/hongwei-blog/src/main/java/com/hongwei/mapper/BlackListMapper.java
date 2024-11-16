package com.hongwei.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongwei.domain.entity.BlackList;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;


/**
 * @author: HongWei
 * @date: 2024/11/16 11:31
 * @param:
 * @return:
 * @description: (BlackList)表数据库访问层
 **/
public interface BlackListMapper extends BaseMapper<BlackList> {

    @Delete("DELETE FROM t_black_list WHERE ip_info -> '$.createIp' = #{ip}")
    Long deleteByIp(String ip);

    // 查询是否存在ip
    @Select("SELECT id FROM t_black_list WHERE ip_info -> '$.createIp' = #{ip}")
    Long getIdByIp(String ip);
}
