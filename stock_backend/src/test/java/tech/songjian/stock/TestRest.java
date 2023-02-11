/**
 * @projectName stock_parent
 * @package tech.songjian.stock
 * @className tech.songjian.stock.TestRest
 */
package tech.songjian.stock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.songjian.stock.service.StockTimerTaskService;

/**
 * TestRest
 * @description
 * @author SongJian
 * @date 2023/2/11 16:20
 * @version
 */
@SpringBootTest
public class TestRest {

    @Autowired
    private StockTimerTaskService stockTimerTaskService;

    @Test
    public void testInner() {
        stockTimerTaskService.collectInnerMarketInfo();
    }

    @Test
    public void TestAShare() {
        stockTimerTaskService.collectAShareInfo();
    }
}

