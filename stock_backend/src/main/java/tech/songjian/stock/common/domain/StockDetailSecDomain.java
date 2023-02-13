/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.StockDetailSecDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;

/**
 * StockDetailSecDomain
 * @description 个股最新分时数据domain
 * @author SongJian
 * @date 2023/2/13 14:04
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDetailSecDomain {
    //最新交易量
    private Long tradeAmt;
    //前收盘价格
    private BigDecimal preClosePrice;
    //最低价
    private BigDecimal lowPrice;
    //最高价
    private BigDecimal highPrice;
    //开盘价
    private BigDecimal openPrice;
    //交易金额
    private BigDecimal tradeVol;
    //当前价格
    private BigDecimal tradePrice;
    //当前日期
    private String curDate;
}

