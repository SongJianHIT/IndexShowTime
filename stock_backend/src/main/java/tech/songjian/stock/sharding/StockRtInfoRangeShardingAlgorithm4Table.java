/**
 * @projectName stock_parent
 * @package tech.songjian.stock.sharding
 * @className tech.songjian.stock.sharding.StockRtInfoRangeShardingAlgorithm4Table
 */
package tech.songjian.stock.sharding;

import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * StockRtInfoRangeShardingAlgorithm4Table
 * @description  为 stock_rt_info 表定义范围匹配类
 * @author SongJian
 * @date 2023/2/15 16:54
 * @version
 */
public class StockRtInfoRangeShardingAlgorithm4Table implements RangeShardingAlgorithm<Date> {
    /**
     *
     * @param tbNames available data sources or tables's names 表名集合
     * @param shardingValue sharding value 封装分片键范围查询对象
     * @return
     */
    @Override
    public Collection<String> doSharding(Collection<String> tbNames, RangeShardingValue<Date> shardingValue) {
        // 获取范围对象
        Range<Date> valueRange = shardingValue.getValueRange();
        // 判断下限
        if (valueRange.hasLowerBound()) {
            // 获取下限值
            Date lowerDate = valueRange.lowerEndpoint();
            String lowDateTime = new DateTime(lowerDate).toString(DateTimeFormat.forPattern("yyyyMM"));
            Integer intDate = Integer.valueOf(lowDateTime);
            // 从集合中获取 大于等于 intDate 的集合
            tbNames = tbNames.stream()
                    .filter(tbname->Integer.valueOf(tbname.substring(tbname.lastIndexOf("_") + 1)) >= intDate)
                    .collect(Collectors.toList());
        }
        // 判断上线
        if (valueRange.hasUpperBound()) {
            // 获取下限值
            Date upperDate = valueRange.upperEndpoint();
            String upDateTime = new DateTime(upperDate).toString(DateTimeFormat.forPattern("yyyyMM"));
            Integer intDate = Integer.valueOf(upDateTime);
            // 从集合中获取 大于等于 intDate 的集合
            tbNames = tbNames.stream()
                    .filter(tbname->Integer.valueOf(tbname.substring(tbname.lastIndexOf("_") + 1)) <= intDate)
                    .collect(Collectors.toList());
        }
        return tbNames;
    }
}

