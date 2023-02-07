/**
 * @projectName stock_parent
 * @package tech.songjian.stock
 * @className tech.songjian.stock.StockApp
 */
package tech.songjian.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * StockApp
 * @description 启动类
 * @author SongJian
 * @date 2023/2/7 15:44
 * @version
 */

@SpringBootApplication
//@MapperScan("tech.songjian.stock.mapper")
public class StockApp {
    public static void main(String[] args) {
        SpringApplication.run(StockApp.class, args);
    }
}

