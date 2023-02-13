/**
 * @projectName stock_parent
 * @package tech.songjian.stock.vo.req
 * @className tech.songjian.stock.vo.req.SetUserInfoVo
 */
package tech.songjian.stock.vo.req;

import lombok.Data;

/**
 * SetUserInfoVo
 * @description
 * @author SongJian
 * @date 2023/2/13 22:46
 * @version
 */
@Data
public class SetUserInfoVo {
    private String id;
    private String username;
    private String phone;
    private String email;
    private String nickName;
    private String realName;
    private Integer sex;
    private Integer createWhere;
    private Integer status;
}

