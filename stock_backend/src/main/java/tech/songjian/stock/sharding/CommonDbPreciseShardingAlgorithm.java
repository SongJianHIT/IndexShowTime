/**
 * @projectName stock_parent
 * @package tech.songjian.stock.sharding
 * @className tech.songjian.stock.sharding.CommonDbPreciseShardingAlgorithm
 */
package tech.songjian.stock.sharding;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Date;

/**
 * CommonDbPreciseShardingAlgorithm
 * @description 定义公共精准匹配数据的类
 * @author SongJian
 * @date 2023/2/15 15:47
 * @version
 */
public class CommonDbPreciseShardingAlgorithm implements PreciseShardingAlgorithm<Date> {
    /**
     *
     * @param availableTargetNames available data sources or tables's names 数据源集合
     * @param shardingValue sharding value 分片键相关信息封装
     * @return  具体的数据源名称
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {
        // 获取分片字段
        String columnName = shardingValue.getColumnName();
        // 获取分片的表
        String logicTableName = shardingValue.getLogicTableName();
        // 获取分片的值
        Date date = shardingValue.getValue();
        // 获取年份
        int year = new DateTime(date).getYear();
        // 在数据源集合中找到，以 year 为结尾的数据源就是目标数据源
        String dsName = availableTargetNames.stream().filter(dbName -> dbName.endsWith(year + "")).findFirst().get();
        return dsName;
    }
}

