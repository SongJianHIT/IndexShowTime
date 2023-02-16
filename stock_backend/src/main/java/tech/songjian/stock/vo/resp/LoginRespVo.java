package tech.songjian.stock.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
/**
 * @author by itheima
 * @Date 2021/12/24
 * @Description 登录后响应前端的vo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRespVo {
    /**
     * 用户ID
     */
    private String id;
    /**
     * 电话
     */
    private String phone;
    /**
     * 用户名
     */
    private String username;
    /**
     * 昵称
     */
    private String nickName;
    /**
     *真实名称
     */
    private String realName;
    /**
     *性别
     */
    private Integer sex;
    /**
     *状态
     */
    private Integer status;
    /**
     *邮件
     */
    private String email;


    private String accessToken;

    private List menus;
    private List permissions;

}
