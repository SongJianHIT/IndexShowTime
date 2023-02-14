/**
 * @projectName stock_parent
 * @package tech.songjian.stock.vo.resp
 * @className tech.songjian.stock.vo.resp.newLoginReqVo
 */
package tech.songjian.stock.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.songjian.stock.common.domain.PermissionDomain;

import java.util.List;

/**
 * newLoginReqVo
 * @description 带权限信息的登入响应vo
 * @author SongJian
 * @date 2023/2/13 16:05
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewLoginReqVo {
    /**
     * 用户id
     */
    private String id;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 真实名称
     */
    private String realName;
    /**
     * 性别
     */
    private Integer sex;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 权限树
     */
    private List<PermissionDomain> menus;
    /**
     * 按钮权限集合
     */
    private List<String> permissions;
}

