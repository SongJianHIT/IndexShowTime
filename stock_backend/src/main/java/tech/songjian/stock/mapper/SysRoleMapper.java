package com.itheima.stock.mapper;

import com.itheima.stock.pojo.SysRole;

/**
 * @Entity com.itheima.stock.pojo.SysRole
 */
public interface SysRoleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysRole record);

    int insertSelective(SysRole record);

    SysRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysRole record);

    int updateByPrimaryKey(SysRole record);

}




