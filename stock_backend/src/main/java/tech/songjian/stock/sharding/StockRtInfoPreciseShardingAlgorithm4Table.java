/**
 * @projectName stock_parent
 * @package tech.songjian.stock.sharding
 * @className tech.songjian.stock.sharding.StockRtInfoPreciseShardingAlgorithm4Table
 */
package tech.songjian.stock.sharding;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collection;
import java.util.Date;

/**
 * StockRtInfoPreciseShardingAlgorithm4Table
 * @description 为 stock_rt_info 表定义精准匹配类
 * @author SongJian
 * @date 2023/2/15 15:43
 * @version
 */
public class StockRtInfoPreciseShardingAlgorithm4Table implements PreciseShardingAlgorithm<Date> {

    /**
     *
     * @param tbNames available data sources or tables's names 表名集合
     * @param shardingValue sharding value 分片键数据的封装
     * @return
     */
    @Override
    public String doSharding(Collection<String> tbNames, PreciseShardingValue<Date> shardingValue) {
        // 获取日期
        Date date = shardingValue.getValue();
        // 获取年月组装的字符串
        String sufixDate = new DateTime(date).toString(DateTimeFormat.forPattern("yyyyMM"));
        // 从 tbNames 中查找以 sufix 结尾的数据
        String name = tbNames.stream().filter(tb -> tb.endsWith(sufixDate)).findFirst().get();
        return name;
    }
}

