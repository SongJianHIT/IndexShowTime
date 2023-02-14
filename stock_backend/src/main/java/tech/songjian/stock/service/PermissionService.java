/**
 * @projectName stock_parent
 * @package tech.songjian.stock.service
 * @className tech.songjian.stock.service.PermissionSsrvice
 */
package tech.songjian.stock.service;

import tech.songjian.stock.common.domain.PermissionDomain;
import tech.songjian.stock.vo.resp.PermissionTreeVo;
import tech.songjian.stock.vo.resp.R;

import java.util.List;

/**
 * PermissionSsrvice
 * @description 权限服务接口
 * @author SongJian
 * @date 2023/2/14 00:18
 * @version
 */
public interface PermissionService {

    /**
     * 树状结构回显权限集合,底层通过递归获取权限数据集合
     * @return
     */
    R<List<PermissionDomain>> getPermissionTree();

    /**
     * 根据用户id查询用户关联权限
     * @param roleId
     * @return
     */
    R<List> getPermissionByUserId(String roleId);
}
