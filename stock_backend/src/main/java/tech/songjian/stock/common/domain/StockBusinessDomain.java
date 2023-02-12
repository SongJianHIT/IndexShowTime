/**
 * @projectName stock_parent
 * @package tech.songjian.stock.common.domain
 * @className tech.songjian.stock.common.domain.StockBusinessDomain
 */
package tech.songjian.stock.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * StockBusinessDomain
 * @description 股票业务domain
 * @author SongJian
 * @date 2023/2/13 00:05
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockBusinessDomain {
    /**
     * 股票编码
     */
    private String code;
    /**
     * 行业
     */
    private String trade;
    /**
     * 公司主营业务
     */
    private String business;
    /**
     * 公司名称
     */
    private String name;
}

