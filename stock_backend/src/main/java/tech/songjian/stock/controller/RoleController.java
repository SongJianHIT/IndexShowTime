/**
 * @projectName stock_parent
 * @package tech.songjian.stock.controller
 * @className tech.songjian.stock.controller.RoleController
 */
package tech.songjian.stock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.songjian.stock.common.domain.OwnRoleAndAllRoleIdsDomain;
import tech.songjian.stock.pojo.SysRole;
import tech.songjian.stock.service.PermissionService;
import tech.songjian.stock.service.RoleService;
import tech.songjian.stock.vo.req.UpdateRoleInfoReq;
import tech.songjian.stock.vo.req.UpdateRolePermissionReq;
import tech.songjian.stock.vo.resp.PageResult;
import tech.songjian.stock.vo.resp.R;

import java.util.List;
import java.util.Map;

/**
 * RoleController
 * @description 角色
 * @author SongJian
 * @date 2023/2/13 19:58
 * @version
 */
@RestController
@RequestMapping("/api")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取用户具有的角色信息，以及所有角色信息
     * @param userId
     * @return
     */
    @GetMapping("/user/roles/{userId}")
    public R<OwnRoleAndAllRoleIdsDomain> getUsersRoles(@PathVariable String userId) {
        return roleService.getUsersRoles(userId);
    }

    /**
     * 更新用户角色信息
     * @param req
     * @return
     */
    @PutMapping("/user/roles")
    public R<String> updateRoleInfo(@RequestBody UpdateRoleInfoReq req) {
        return roleService.updateRoleInfo(req);
    }

    /**
     * 分页查询当前角色信息
     * @return
     */
    @PostMapping("/roles")
    public R<PageResult<SysRole>> getRolesInfoByPage(@RequestBody Map<String, Integer> pageInfo) {
        return roleService.getRolesInfoByPage(pageInfo.get("pageNum"), pageInfo.get("pageSize"));
    }

    /**
     * 根据用户id查询用户关联权限
     * @param roleId
     * @return
     */
    @GetMapping("/role/{roleId}")
    public R<List> getPermissionByUserId(@PathVariable String roleId) {
        return permissionService.getPermissionByUserId(roleId);
    }

    /**
     * 更新角色信息（包括权限信息）
     * @param updateRolePermissionReq
     * @return
     */
    @PutMapping("/role")
    public R<String> updatePermissionByRoleId(@RequestBody UpdateRolePermissionReq updateRolePermissionReq) {
        return roleService.updatePermissionByRoleId(updateRolePermissionReq);
    }

    /**
     * 删除角色和角色关联的权限
     * @param roleId
     * @return
     */
    @DeleteMapping("/role/{roleId}")
    public R<String> deleteRoleAndPerByRoleId(@PathVariable String roleId) {
        return roleService.deleteRoleAndPerByRoleId(roleId);
    }

    /**
     * 根据角色id更新角色状态
     * @param roleId
     * @param status
     * @return
     */
    @PostMapping("/role/{roleId}/{status}")
    public R<String> updateRoleStatus(@PathVariable String roleId, @PathVariable String status) {
        return roleService.updateRoleStatus(roleId, status);
    }



}

