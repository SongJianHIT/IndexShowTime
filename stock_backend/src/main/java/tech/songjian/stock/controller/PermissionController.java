/**
 * @projectName stock_parent
 * @package tech.songjian.stock.controller
 * @className tech.songjian.stock.controller.PermissionController
 */
package tech.songjian.stock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.songjian.stock.common.domain.PermissionDomain;
import tech.songjian.stock.service.PermissionService;
import tech.songjian.stock.vo.resp.PermissionTreeVo;
import tech.songjian.stock.vo.resp.R;

import java.util.List;

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

}

