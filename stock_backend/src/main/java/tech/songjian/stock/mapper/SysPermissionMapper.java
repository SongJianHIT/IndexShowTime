package com.itheima.stock.mapper;

import com.itheima.stock.pojo.SysPermission;

/**
 * @Entity com.itheima.stock.pojo.SysPermission
 */
public interface SysPermissionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysPermission record);

    int insertSelective(SysPermission record);

    SysPermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysPermission record);

    int updateByPrimaryKey(SysPermission record);

}




