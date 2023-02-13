/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.OwnRoleAndAllRoleIdsDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.songjian.stock.pojo.SysRole;

import java.util.List;

/**
 * OwnRoleAndAllRoleIdsDomain
 * @description 用户角色信息domain
 * @author SongJian
 * @date 2023/2/13 20:05
 * @version
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OwnRoleAndAllRoleIdsDomain {
    // 用户具有的角色id
    private List<String> ownRoleIds;
    // 所有角色的信息
    private List<SysRole> allRole;
}

