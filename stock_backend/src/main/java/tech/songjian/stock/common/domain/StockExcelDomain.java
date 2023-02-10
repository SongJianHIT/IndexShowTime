/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.StockUpdownDomain
 */
package tech.songjian.stock.common.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * StockExcelDomain
 * @description 导出股票信息的实体类对象
 * @author SongJian
 * @date 2023/2/10 11:11
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockExcelDomain {

    /**
     * 股票编码
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "股票编码"}, index = 0)
    private String code;
    /**
     * 股票名称
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "股票名称"}, index = 1)
    private String name;
    /**
     * 前收盘价
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "前收盘价格"}, index = 2)
    private BigDecimal preClosePrice;
    /**
     * 当前价格
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "当前价格"}, index = 3)
    private BigDecimal tradePrice;
    /**
     * 涨跌
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "涨跌"}, index = 4)
    private BigDecimal increase;
    /**
     * 涨跌幅度
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "涨跌幅度"}, index = 5)
    private BigDecimal upDown;
    /**
     * 振幅
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "振幅"}, index = 6)
    private BigDecimal amplitude;
    /**
     * 交易量
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "交易总量"}, index = 7)
    private Long tradeAmt;
    /**
     * 交易金额
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "交易总金额"}, index = 8)
    private BigDecimal tradeVol;
    /**
     * 日期
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "日期"}, index = 9)
    private String curDate;
}
