/**
 * @projectName stock_parent
 * @package tech.songjian.stock.vo.resp
 * @className tech.songjian.stock.vo.resp.PermissionTreeVo
 */
package tech.songjian.stock.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.songjian.stock.common.domain.PermissionDomain;

import java.util.List;

/**
 * PermissionTreeVo
 * @description 树状权限回显
 * @author SongJian
 * @date 2023/2/14 00:13
 * @version
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionTreeVo {
    /**
     * 权限id
     */
    private String id;
    /**
     * 权限标题
     */
    private String title;
    /**
     * 权限图标
     */
    private String icon;
    /**
     * 权限路径
     */
    private String path;
    /**
     * name与前端vue路由name约定一致
     */
    private String name;
    /**
     * 子权限
     */
    private List<PermissionTreeVo> children;
}

