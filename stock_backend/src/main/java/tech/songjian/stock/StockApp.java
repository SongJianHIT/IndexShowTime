/**
 * @projectName stock_parent
 * @package tech.songjian.stock
 * @className tech.songjian.stock.StockApp
 */
package tech.songjian.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tech.songjian.stock.config.vo.StockInfoConfig;
import tech.songjian.stock.config.vo.TaskThreadPollInfo;

/**
 * StockApp
 * @description 启动类
 * @author SongJian
 * @date 2023/2/7 15:44
 * @version
 */

@SpringBootApplication
@EnableConfigurationProperties({StockInfoConfig.class, TaskThreadPollInfo.class})   // 开启配置初始化，加入IOC容器
//@MapperScan("tech.songjian.stock.mapper")
public class StockApp {
    public static void main(String[] args) {
        SpringApplication.run(StockApp.class, args);
    }
}
