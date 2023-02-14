package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.pojo.SysRole;

import java.util.Date;
import java.util.List;

/**
 * @Entity tech.songjian.stock.pojo.SysRole
 */
@Mapper
public interface SysRoleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysRole record);

    int insertSelective(SysRole record);

    SysRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysRole record);

    int updateByPrimaryKey(SysRole record);

    /**
     * 查询所有角色
     * @return
     */
    List<SysRole> getAllRoles();

    /**
     * 查询所有角色信息
     * @return
     */
    List<SysRole> getRolesInfo();

    /**
     * 根据角色id修改角色名称和角色描述
     * @param name
     * @param description
     */
    void updateNameAndDesById(@Param("name") String name,
                              @Param("description") String description,
                              @Param("id") String id,
                              @Param("updateTime") Date updateTime);

    /**
     * 根据角色id删除角色
     * @param roleId
     */
    void deleteByRoleId(String roleId);
}




