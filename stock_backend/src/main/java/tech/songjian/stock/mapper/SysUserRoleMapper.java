package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.pojo.SysRole;
import tech.songjian.stock.pojo.SysUserRole;
import tech.songjian.stock.vo.resp.PageResult;

import java.util.Date;
import java.util.List;

/**
 * @Entity tech.songjian.stock.pojo.SysUserRole
 */
@Mapper
public interface SysUserRoleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysUserRole record);

    int insertSelective(SysUserRole record);

    SysUserRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysUserRole record);

    int updateByPrimaryKey(SysUserRole record);

    /**
     * 根据用户id查询角色id
     * @param userId
     * @return
     */
    List<String> queryRolesById(@Param("userId") String userId);

    /**
     * 根据用户 id 删除
     * @param userId
     */
    void deleteByUserId(@Param("userId") String userId);

    /**
     * 插入条目
     * @param primaryKey
     * @param userId
     * @param roleId
     * @param updateTime
     */
    void inserByUserRoleIds(@Param("primaryKey") long primaryKey, @Param("userId") String userId,
                            @Param("roleId") String roleId, @Param("updateTime") Date updateTime);



}




