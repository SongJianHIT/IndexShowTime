/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.StockExternalIndexDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * StockExternalIndexDomain
 * @description 外盘行情do对象
 * @author SongJian
 * @date 2023/2/12 21:24
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockExternalIndexDomain {
    /**
     * 当前点数
     */
    private BigDecimal curPoint;
    /**
     * 当前日期
     */
    private String curTime;
    /**
     * 大盘名称
     */
    private String name;
    /**
     * 交易金额
     */
    private BigDecimal tradePrice;
    /**
     * 涨幅
     */
    private BigDecimal updownRate;
}

