package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.common.domain.InnerMarketDomain;
import tech.songjian.stock.common.domain.StockExternalIndexDomain;
import tech.songjian.stock.pojo.StockMarketIndexInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Entity tech.songjian.stock.pojo.StockMarketIndexInfo
 */
@Mapper
public interface StockMarketIndexInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockMarketIndexInfo record);

    int insertSelective(StockMarketIndexInfo record);

    StockMarketIndexInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockMarketIndexInfo record);

    int updateByPrimaryKey(StockMarketIndexInfo record);

    /**
     * 根据大盘 id 和 时间 查询大盘信息
     * @param marketIds 大盘 id 集合
     * @param timePoint 当前时间点
     * @return
     */
    List<InnerMarketDomain> getMarketInfo(@Param("marketIds") List<String> marketIds, @Param("timePoint") Date timePoint);

    /**
     * 根据指定的大盘id集合和时间范围，统计每分钟的交易量
     * @param marketIds 大盘id集合
     * @param startTime 交易开始时间
     * @param endTime 结束时间
     * @return
     */
    List<Map> getStockTradeVol(@Param("marketIds") List<String> marketIds, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 批量插入大盘数据
     * @param list
     * @return
     */
    int insertBatch(@Param("stockMarketInfoList") List<StockMarketIndexInfo> infos);

    /**
     * 外盘指数行情数据查询，根据时间和大盘点数降序排序取前4
     * @param marketIds
     * @param date
     * @return
     */
    List<StockExternalIndexDomain> getExternalIndexInfoTop4(@Param("marketIds") List<String> marketIds, @Param("timePoint") Date timePoint);
}




