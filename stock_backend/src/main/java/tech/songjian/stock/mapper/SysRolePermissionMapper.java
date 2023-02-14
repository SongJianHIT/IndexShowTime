package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.pojo.SysRolePermission;

import java.util.Date;
import java.util.List;

/**
 * @Entity tech.songjian.stock.pojo.SysRolePermission
 */
@Mapper
public interface SysRolePermissionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysRolePermission record);

    int insertSelective(SysRolePermission record);

    SysRolePermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysRolePermission record);

    int updateByPrimaryKey(SysRolePermission record);

    /**
     * 根据用户id 查询权限集合
     * @param roleId
     * @return
     */
    List<String> getPermissionByUserId(@Param("roleId") String roleId);

    /**
     * 根据roleid删除权限
     * @param roleId
     */
    void deleteByRoleId(@Param("roleId") String roleId);

}




