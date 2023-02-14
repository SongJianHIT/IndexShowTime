/**
 * @projectName stock_parent
 * @package tech.songjian.stock.controller
 * @className tech.songjian.stock.controller.PermissionController
 */
package tech.songjian.stock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.songjian.stock.common.domain.PermissionDomain;
import tech.songjian.stock.pojo.SysPermission;
import tech.songjian.stock.service.PermissionService;
import tech.songjian.stock.vo.resp.PermissionTreeLevelResp;
import tech.songjian.stock.vo.resp.PermissionTreeVo;
import tech.songjian.stock.vo.resp.R;

import java.util.List;
import java.util.Map;

/**
 * PermissionController
 * @description 权限
 * @author SongJian
 * @date 2023/2/14 00:06
 * @version
 */
@RestController
@RequestMapping("/api")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 树状结构回显权限集合,底层通过递归获取权限数据集合
     * @return
     */
    @GetMapping("/permissions/tree/all")
    public R<List<PermissionDomain>> getPermissionTree() {
        return permissionService.getPermissionTree();
    }

    /**
     * 获取所有权限集合
     * @return
     */
    @GetMapping("/permissions")
    public R<List<SysPermission>> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    /**
     * 添加权限时回显权限树，仅仅显示目录和菜单
     * @return
     */
    @GetMapping("/permissions/tree")
    public R<List<Map>> getPermissionsTree4Add() {
        return permissionService.getPermissionsTree4Add();
    }

    /**
     * 权限添加按钮
     * @param sysPermission
     * @return
     */
    @PostMapping("/permission")
    public R<String> addPermission(@RequestBody SysPermission sysPermission){
        return permissionService.addPermission(sysPermission);
    }
}

