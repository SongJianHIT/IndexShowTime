/**
 * @projectName stock_parent
 * @package tech.songjian.stock.job
 * @className tech.songjian.stock.job.StockTimerJob
 */
package tech.songjian.stock.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.songjian.stock.service.StockTimerTaskService;

/**
 * StockTimerJob
 * @description 配置xxljob执行任务的bean对象
 * @author SongJian
 * @date 2023/2/12 10:19
 * @version
 */
@Component
public class StockTimerJob {

    @Autowired
    private StockTimerTaskService stockTimerTaskService;

    /**
     * 测试
     */
    @XxlJob("testXxlJob")
    public void testXxlJob() {
        System.out.println("testXxlJob Run .....");
    }

    /**
     * 获取国内大盘数据
     * 周一至周五，上午的9：30到11：30，下午的1：00-3：00
     */
    @XxlJob("getInnerMarketInfo")
    public void getInnerMarketInfo() {
        stockTimerTaskService.collectInnerMarketInfo();
    }

    /**
     * 获取A股个股信息
     */
    @XxlJob("collectAShareInfo")
    public void collectAShareInfo() {
        stockTimerTaskService.collectAShareInfo();
    }

    /**
     * 获取板块数据
     */
    @XxlJob("getStockSectorRtIndex")
    public void getStockSectorRtIndex() {
        stockTimerTaskService.getStockSectorRtIndex();
    }
}
