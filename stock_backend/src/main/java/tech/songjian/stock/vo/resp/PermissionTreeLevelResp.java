/**
 * @projectName stock_parent
 * @package tech.songjian.stock.vo.resp
 * @className tech.songjian.stock.vo.resp.PermissionTreeLevelResp
 */
package tech.songjian.stock.vo.resp;

import lombok.Data;

/**
 * PermissionTreeLevelResp
 * @description 回显权限树响应VO
 * @author SongJian
 * @date 2023/2/14 17:30
 * @version
 */
@Data
public class PermissionTreeLevelResp {
    private String id;
    private String title;
    private Integer level;
}

