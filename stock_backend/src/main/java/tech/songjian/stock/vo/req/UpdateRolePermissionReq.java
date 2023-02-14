/**
 * @projectName stock_parent
 * @package tech.songjian.stock.vo.req
 * @className tech.songjian.stock.vo.req.UpdateRolePermissionReq
 */
package tech.songjian.stock.vo.req;

import lombok.Data;

import java.util.List;

/**
 * UpdateRolePermissionReq
 * @description 更新角色信息vo
 * @author SongJian
 * @date 2023/2/14 14:28
 * @version
 */
@Data
public class UpdateRolePermissionReq {

    /**
     * 角色id
     */
    private String id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 权限集合
     */
    private List<String> permissionsIds;

}

