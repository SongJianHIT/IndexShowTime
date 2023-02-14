/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.PermissionDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PermissionDomain
 * @description 权限domain
 * @author SongJian
 * @date 2023/2/13 16:06
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDomain {
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
     * 请求地址
     */
    private String path;
    /**
     * 权限名称对应前端vue组件名称
     */
    private String name;
    /**
     * 此id对应的子权限的集合
     */
    private List<PermissionDomain> children;
}

