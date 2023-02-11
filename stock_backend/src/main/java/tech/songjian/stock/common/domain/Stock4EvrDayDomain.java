/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.Stock4EvrDayDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Stock4EvrDayDomain
 * @description 以天为单位统计个股数据
 * @author SongJian
 * @date 2023/2/11 11:41
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock4EvrDayDomain {
    /**
     * 日期，eg:202201280809
     */
    private String date;
    /**
     * 交易量
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
     * 当前交易总金额
     */
    private BigDecimal tradeVol;
    /**
     * 当前收盘价格指收盘时的价格，如果当天未收盘，则显示最新cur_price）
     */
    private BigDecimal closePrice;
    /**
     * 前收盘价
     */
    private BigDecimal preClosePrice;
}

