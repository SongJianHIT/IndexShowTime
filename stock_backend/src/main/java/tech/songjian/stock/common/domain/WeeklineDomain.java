/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.WeekLineDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * WeekLineDomain
 * @description
 * @author SongJian
 * @date 2023/2/13 11:32
 * @version
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklineDomain {
    private BigDecimal avgPrice; // 一周内平均价
    private BigDecimal minPrice; // 一周内最低价
    private BigDecimal openPrice; // 周一开盘价
    private BigDecimal maxPrice; // 一周内最高价
    private BigDecimal closePrice; // 周五收盘价(如果当前日期不到周五,则显示最新价格)
    private String mxTime; // 一周内的最大时间
    private String stock_code; // 股票编码
}

