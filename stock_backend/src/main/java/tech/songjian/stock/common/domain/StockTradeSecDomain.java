/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.StockTradeSecDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * StockTradeSecDomain
 * @description 股票交易流水domain
 * @author SongJian
 * @date 2023/2/13 14:27
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTradeSecDomain {
    //当前时间，精确到分
    private String date;
    //交易量
    private Long tradeAmt;
    //交易金额
    private BigDecimal tradeVol;
    //交易价格
    private BigDecimal tradePrice;
}

