/**
 * @projectName stock_parent
 * @package tech.songjian.stock.controller
 * @className tech.songjian.stock.controller.RoleController
 */
package tech.songjian.stock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.songjian.stock.common.domain.OwnRoleAndAllRoleIdsDomain;
import tech.songjian.stock.service.RoleService;
import tech.songjian.stock.vo.req.UpdateRoleInfoReq;
import tech.songjian.stock.vo.resp.R;

import java.util.Map;

/**
 * RoleController
 * @description 角色
 * @author SongJian
 * @date 2023/2/13 19:58
 * @version
 */
@RestController
@RequestMapping("/api/user")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取用户具有的角色信息，以及所有角色信息
     * @param userId
     * @return
     */
    @GetMapping("/roles/{userId}")
    public R<OwnRoleAndAllRoleIdsDomain> getUsersRoles(@PathVariable String userId) {
        return roleService.getUsersRoles(userId);
    }

    /**
     * 更新用户角色信息
     * @param req
     * @return
     */
    @PutMapping("/roles")
    public R<String> updateRoleInfo(@RequestBody UpdateRoleInfoReq req) {
        return roleService.updateRoleInfo(req);
    }
}

