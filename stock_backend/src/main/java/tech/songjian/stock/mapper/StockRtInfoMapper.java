package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.common.domain.Stock4EvrDayDomain;
import tech.songjian.stock.common.domain.Stock4MinuteDomain;
import tech.songjian.stock.common.domain.StockUpdownDomain;
import tech.songjian.stock.pojo.StockRtInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Entity tech.songjian.stock.pojo.StockRtInfo
 */
@Mapper
public interface StockRtInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockRtInfo record);

    int insertSelective(StockRtInfo record);

    StockRtInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockRtInfo record);

    int updateByPrimaryKey(StockRtInfo record);

    /**
     * 查询指定时间点下数据，根据涨幅取前十
     * @param timePoint 时间点，分钟级别
     * @return
     */
    List<StockUpdownDomain> getStockRtInfoLimit(@Param("timePoint") Date timePoint);

    /**
     * 根据日期和涨幅降序查询股票信息
     * @return
     */
    List<StockUpdownDomain> getStockRtInfo4All();

    /**
     * 根据指定日期时间范围，统计对应范围内每分钟的涨停或者跌停数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param flag 标识。 1：涨停   0：跌停
     * @return
     */
    List<Map> getStockUpdownCount(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("flag") int flag);

    /**
     * 统计指定时间点下，各个涨跌区间内股票的个数
     * @param timePoint 股票交易时间点
     * @return
     */
    List<Map> getStockUpDownRegion(Date timePoint);

    /**
     * 查询个股的分时行情数据，也就是统计指定股票T日每分钟的交易数据；
     * @param stockCode 股票编码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    List<Stock4MinuteDomain> getStockInfoByCodeAndDate(@Param("stockCode") String stockCode, @Param("startTime") Date startTime, @Param("endTime") Date endTime);


    /**
     * 个股日K数据查询 ，可以根据时间区间查询数日的K线数据
     * @param stockCode
     * @param startTime
     * @param endTime
     * @return
     */
    List<Stock4EvrDayDomain> getStockRtInfo4EvrDat(@Param("stockCode") String stockCode, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 批量保存股票详情数据
     * @param infos
     * @return
     */
    int insertBatch(@Param("stockRtInfoList") List<StockRtInfo> infos);
}
