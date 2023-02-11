/**
 * @projectName stock_parent
 * @package tech.songjian.stock.config.vo
 * @className tech.songjian.stock.config.vo.StockInfoConfig
 */
package tech.songjian.stock.config.vo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * StockInfoConfig
 * @description 股票常量数据封装，从yml中读取
 * @author SongJian
 * @date 2023/2/9 20:11
 * @version
 */
@Data
@ConfigurationProperties(prefix = "stock")
public class StockInfoConfig {
    /**
     * A股code集合
     */
    private List<String> inner;
    /**
     * 外股code集合
     */
    private List<String> outer;
    /**
     * 定义股票涨幅区间的顺序
     */
    private List<String> upDownRange;
}

