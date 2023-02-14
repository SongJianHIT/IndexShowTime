/**
 * @projectName stock_parent
 * @package tech.songjian.stock.service
 * @className tech.songjian.stock.service.RoleService
 */
package tech.songjian.stock.service;

import tech.songjian.stock.common.domain.OwnRoleAndAllRoleIdsDomain;
import tech.songjian.stock.pojo.SysRole;
import tech.songjian.stock.vo.req.UpdateRoleInfoReq;
import tech.songjian.stock.vo.req.UpdateRolePermissionReq;
import tech.songjian.stock.vo.resp.PageResult;
import tech.songjian.stock.vo.resp.R;

/**
 * RoleService
 * @description 角色服务接口
 * @author SongJian
 * @date 2023/2/13 20:00
 * @version
 */
public interface RoleService {

    /**
     * 获取用户具有的角色信息，以及所有角色信息
     * @param userId
     * @return
     */
    R<OwnRoleAndAllRoleIdsDomain> getUsersRoles(String userId);

    /**
     * 更新用户角色信息
     * @param req
     * @return
     */
    R<String> updateRoleInfo(UpdateRoleInfoReq req);

    /**
     * 分页查询角色信息
     * @param pageNum
     * @param pageSize
     * @return
     */
    R<PageResult<SysRole>> getRolesInfoByPage(Integer pageNum, Integer pageSize);

    /**
     * 更新角色信息（包括权限信息）
     * @param updateRolePermissionReq
     * @return
     */
    R<String> updatePermissionByRoleId(UpdateRolePermissionReq updateRolePermissionReq);
}
