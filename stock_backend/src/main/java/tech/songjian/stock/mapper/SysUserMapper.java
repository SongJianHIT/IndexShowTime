package com.itheima.stock.mapper;

import com.itheima.stock.pojo.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.itheima.stock.pojo.SysUser
 */
@Mapper
public interface SysUserMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    SysUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);

    /**
     * 根据用户名称查询用户信息
     * @param username
     * @return
     */
    SysUser findUserInfoByUserName(@Param("username") String username);
}




