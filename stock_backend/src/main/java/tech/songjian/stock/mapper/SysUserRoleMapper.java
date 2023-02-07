package tech.songjian.stock.mapper;

import tech.songjian.stock.pojo.SysUserRole;

/**
 * @Entity tech.songjian.stock.pojo.SysUserRole
 */
public interface SysUserRoleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysUserRole record);

    int insertSelective(SysUserRole record);

    SysUserRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysUserRole record);

    int updateByPrimaryKey(SysUserRole record);

}




