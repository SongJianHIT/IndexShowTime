package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.common.domain.StockUpdownDomain;
import tech.songjian.stock.pojo.StockRtInfo;

import java.util.Date;
import java.util.List;

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
}




