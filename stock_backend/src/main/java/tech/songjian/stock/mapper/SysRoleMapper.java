package tech.songjian.stock.mapper;

import tech.songjian.stock.pojo.SysRole;

/**
 * @Entity tech.songjian.stock.pojo.SysRole
 */
public interface SysRoleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysRole record);

    int insertSelective(SysRole record);

    SysRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysRole record);

    int updateByPrimaryKey(SysRole record);

}




