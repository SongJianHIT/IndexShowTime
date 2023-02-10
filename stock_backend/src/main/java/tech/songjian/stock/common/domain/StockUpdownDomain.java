/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.StockUpdownDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * StockUpdownDomain
 * @description 股票涨跌信息
 * @author SongJian
 * @date 2023/2/10 11:11
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdownDomain {
    /**
     * 交易量
     */
    private Long tradeAmt;
    /**
     * 前收盘价
     */
    private BigDecimal preClosePrice;
    /**
     * 振幅
     */
    private BigDecimal amplitude;
    /**
     * 股票编码
     */
    private String code;
    /**
     * 股票名称
     */
    private String name;
    /**
     * 日期
     */
    private String curDate;
    /**
     * 交易金额
     */
    private BigDecimal tradeVol;
    /**
     * 涨跌
     */
    private BigDecimal upDown;
    /**
     * 涨跌幅度
     */
    private BigDecimal increase;
    /**
     * 当前价格
     */
    private BigDecimal tradePrice;
}

