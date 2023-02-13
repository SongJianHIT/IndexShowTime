/**
 * @projectName stock_parent
 * @package tech.songjian.stock.service.impl
 * @className tech.songjian.stock.service.impl.RoleServiceImpl
 */
package tech.songjian.stock.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.songjian.stock.common.domain.OwnRoleAndAllRoleIdsDomain;
import tech.songjian.stock.mapper.SysRoleMapper;
import tech.songjian.stock.mapper.SysUserRoleMapper;
import tech.songjian.stock.pojo.SysRole;
import tech.songjian.stock.pojo.SysUserRole;
import tech.songjian.stock.service.RoleService;
import tech.songjian.stock.vo.resp.R;

import java.util.List;

/**
 * RoleServiceImpl
 * @description
 * @author SongJian
 * @date 2023/2/13 20:01
 * @version
 */
@Service("roleService")
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 获取用户具有的角色信息，以及所有角色信息
     * @param userId
     * @return
     */
    @Override
    public R<OwnRoleAndAllRoleIdsDomain> getUsersRoles(String userId) {
        OwnRoleAndAllRoleIdsDomain domain = new OwnRoleAndAllRoleIdsDomain();
        List<SysRole> allRoles = sysRoleMapper.getAllRoles();
        List<String> ids = sysUserRoleMapper.queryRolesById(userId);
        domain.setAllRole(allRoles);
        domain.setOwnRoleIds(ids);
        return R.ok(domain);
    }
}

