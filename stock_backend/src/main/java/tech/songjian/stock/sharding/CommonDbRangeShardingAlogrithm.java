/**
 * @projectName stock_parent
 * @package tech.songjian.stock.sharding
 * @className tech.songjian.stock.sharding.CommonDbRangeShardingAlogrithm
 */
package tech.songjian.stock.sharding;

import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * CommonDbRangeShardingAlogrithm
 * @description 定义公共范围匹配数据的类
 * @author SongJian
 * @date 2023/2/15 15:54
 * @version
 */
public class CommonDbRangeShardingAlogrithm implements RangeShardingAlgorithm<Date> {
    /**
     *
     * @param availableTargetNames available data sources or tables's names 数据源的名称集合
     * @param shardingValue sharding value 范围查询片键值的封装
     * @return
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Date> shardingValue) {
        // 获取范围封装对象
        Range<Date> valueRange = shardingValue.getValueRange();
        // 判断是否有下限制
        if (valueRange.hasLowerBound()) {
            Date lowerDate = valueRange.lowerEndpoint();
            // 获取年份
            int lowerYear = new DateTime(lowerDate).getYear();
            availableTargetNames = availableTargetNames.stream()
                    .filter(dsName->Integer.valueOf(dsName.substring(dsName.lastIndexOf("-") + 1)) >= lowerYear)
                    .collect(Collectors.toList());
        }
        // 判断是否有上限制
        if (valueRange.hasUpperBound()) {
            Date upperDate = valueRange.upperEndpoint();
            int upperYear = new DateTime(upperDate).getYear();
            availableTargetNames = availableTargetNames.stream()
                    .filter(dsName->Integer.valueOf(dsName.substring(dsName.lastIndexOf("-") + 1)) <= upperYear)
                    .collect(Collectors.toList());
        }
        return availableTargetNames;
    }
}

