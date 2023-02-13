/**
 * @projectName stock_parent
 * @package tech.songjian.stock.vo.resp
 * @className tech.songjian.stock.vo.resp.ConditionQueryUserResp
 */
package tech.songjian.stock.vo.resp;

import lombok.Data;
import tech.songjian.stock.pojo.SysUser;

import java.util.List;

/**
 * ConditionQueryUserResp
 * @description 多条件查询用户VO
 * @author SongJian
 * @date 2023/2/13 17:09
 * @version
 */
@Data
public class ConditionQueryUserResp {

    private Long totalRows;
    private Integer totalPages;
    private Integer pageNum;
    private Integer pageSize;
    private Integer size;
    private List<SysUser> rows;
}

