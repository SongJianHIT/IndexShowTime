package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.pojo.SysPermission;
import tech.songjian.stock.vo.resp.PermissionTreeVo;

import java.util.List;
import java.util.Map;

/**
 * @Entity tech.songjian.stock.pojo.SysPermission
 */
@Mapper
public interface SysPermissionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysPermission record);

    int insertSelective(SysPermission record);

    SysPermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysPermission record);

    int updateByPrimaryKey(SysPermission record);

    /**
     * 查询所有权限信息，封装至PermissionTreeVo
     * @return
     */
    List<SysPermission> getAllPermission();

    /**
     * 获取所有权限集合
     * @return
     */
    List<SysPermission> getAllPermissions();


    List<Map> findAllPermissionSon(String id);

    List<Map> findAllPermissionlLevel1();

    /**
     * 添加权限
     * @param sysPermission
     * @return
     */
    int addPermission(@Param("sysPermission") SysPermission sysPermission);

    /**
     * 根据id删除
     * @param permissionId
     * @return
     */
    int deletePermission(@Param("permissionId") String permissionId);

    /**
     * 根据用户id查询权限集合
     * @param id
     * @return
     */
    List<SysPermission> getPermissionByUserId(@Param("id") String id);
}




