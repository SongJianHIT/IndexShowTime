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
    private String id; // 用户id
    private String username; // 用户名称
    private String phone; // 手机号
    private String nickName; // 昵称
    private String realName;// 真实名称
    private Integer sex; // 性别
    private Integer status; // 状态
    private String email; // 邮箱
    private List<PermissionDomain> menus; //权限树
    private List<String> permissions; // 按钮权限集合
}

