/**
 * @projectName stock_parent
 * @package tech.songjian.stock.service
 * @className tech.songjian.stock.service.RoleService
 */
package tech.songjian.stock.service;

import tech.songjian.stock.common.domain.OwnRoleAndAllRoleIdsDomain;
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
}
