/**
 * @projectName stock_parent
 * @package tech.songjian.stock.vo.req
 * @className tech.songjian.stock.vo.req.UpdateRoleInfoReq
 */
package tech.songjian.stock.vo.req;

import lombok.Data;

import java.util.List;

/**
 * UpdateRoleInfoReq
 * @description 更新用户角色信息vo
 * @author SongJian
 * @date 2023/2/13 21:19
 * @version
 */
@Data
public class UpdateRoleInfoReq {
    private String userId;
    private List<String> roleIds;
}

