package tech.songjian.stock.mapper;

import tech.songjian.stock.pojo.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.vo.req.ConditionalQueryUserReq;

import java.util.List;

/**
 * @Entity tech.songjian.stock.pojo.SysUser
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

    /**
     * 根据用户名查询用户信息和权限信息
     * @param username
     * @return
     */
    SysUser getUserPermissionInfo(String username);


    /**
     * 多条件综合查询用户分页信息，条件包含：分页信息 用户创建日期范围
     * @param req
     * @return
     */
    List<SysUser> conditionQueryUser(@Param("req") ConditionalQueryUserReq req);

    /**
     * 添加用户
     * @param user
     * @return
     */
    int insertUser(@Param("user") SysUser user);
}




