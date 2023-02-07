package tech.songjian.stock.vo.req;

import lombok.Data;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description 用户登录请求vo
 */
@Data
public class LoginReqVo {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 前端发送的验证码
     */
    private String code;
}
