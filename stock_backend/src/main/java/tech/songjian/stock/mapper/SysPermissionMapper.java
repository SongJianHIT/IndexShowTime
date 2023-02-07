package tech.songjian.stock.mapper;

import tech.songjian.stock.pojo.SysPermission;

/**
 * @Entity tech.songjian.stock.pojo.SysPermission
 */
public interface SysPermissionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysPermission record);

    int insertSelective(SysPermission record);

    SysPermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysPermission record);

    int updateByPrimaryKey(SysPermission record);

}




