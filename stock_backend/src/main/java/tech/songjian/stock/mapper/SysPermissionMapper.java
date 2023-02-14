package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import tech.songjian.stock.pojo.SysPermission;
import tech.songjian.stock.vo.resp.PermissionTreeVo;

import java.util.List;

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

}




