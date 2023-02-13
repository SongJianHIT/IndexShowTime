/**
 * @projectName stock_parent
 * @package tech.songjian.stock.vo.req
 * @className tech.songjian.stock.vo.req.ConditionQueryUserReq
 */
package tech.songjian.stock.vo.req;

import lombok.Data;

/**
 * ConditionQueryUserReq
 * @description 多条件查询用户信息请求vo
 * @author SongJian
 * @date 2023/2/13 17:11
 * @version
 */
@Data
public class ConditionalQueryUserReq {
    private String pageNum;
    private String pageSize;
    private String username;
    private String nickName;
    private String startTime;
    private String endTime;
}

