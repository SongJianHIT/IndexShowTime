/**
 * @projectName stock_parent
 * @package tech.songjian.stock.vo.resp
 * @className tech.songjian.stock.vo.resp.GetUserInfoVo
 */
package tech.songjian.stock.vo.resp;

import lombok.Data;

/**
 * GetUserInfoVo
 * @description
 * @author SongJian
 * @date 2023/2/13 22:12
 * @version
 */
@Data
public class GetUserInfoVo {
    /**
     * 用户id
     */
    private String id;
    /**
     * 账户
     */
    private String username;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 真实名称
     */
    private String realName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 邮箱(唯一)
     */
    private String email;

    /**
     * 账户状态(1.正常 2.锁定 )
     */
    private Integer status;

    /**
     * 性别(1.男 2.女)
     */
    private Integer sex;
}

