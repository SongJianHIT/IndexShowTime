/**
 * @projectName stock_parent
 * @package tech.songjian.stock.service
 * @className tech.songjian.stock.service.StockTimerTaskService
 */
package tech.songjian.stock.service;

/**
 * StockTimerTaskService
 * @description 定时采集股票数据服务接口
 * @author SongJian
 * @date 2023/2/11 16:09
 * @version
 */

public interface StockTimerTaskService {
    /**
     * 获取国内大盘的实时数据信息
     */
    void collectInnerMarketInfo();

    /**
     * 采集国内 A 股 股票详情信息
     */
    void collectAShareInfo();

    /**
     * 获取板块数据
     */
    void getStockSectorRtIndex();
}

