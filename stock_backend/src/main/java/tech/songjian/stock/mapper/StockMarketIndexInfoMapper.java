package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.common.domain.InnerMarketDomain;
import tech.songjian.stock.pojo.StockMarketIndexInfo;

import java.util.Date;
import java.util.List;

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
}




