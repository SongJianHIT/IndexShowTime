/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.Stock4MinuteDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Stock4MinuteDomain
 * @description 股票详情DO
 * @author SongJian
 * @date 2023/2/11 10:54
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock4MinuteDomain {
    /**
     * 日期，eg:202201280809
     */
    private String date;
    /**
     * 交易金额
     */
    private Long tradeAmt;
    /**
     * 股票编码
     */
    private String code;
    /**
     * 最低价
     */
    private BigDecimal lowPrice;
    /**
     * 前收盘价
     */
    private BigDecimal preClosePrice;
    /**
     * 股票名称
     */
    private String name;
    /**
     * 最高价
     */
    private BigDecimal highPrice;
    /**
     * 开盘价
     */
    private BigDecimal openPrice;

    /**
     * 交易量
     */
    private BigDecimal tradeVol;
    /**
     * 当前价格
     */
    private BigDecimal tradePrice;
}

