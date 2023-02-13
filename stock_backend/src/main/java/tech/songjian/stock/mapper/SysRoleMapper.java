package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import tech.songjian.stock.pojo.SysRole;

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


}




