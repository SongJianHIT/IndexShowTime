package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.pojo.StockBlockRtInfo;

import java.util.List;

/**
 * @Entity tech.songjian.stock.pojo.StockBlockRtInfo
 */
@Mapper
public interface StockBlockRtInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockBlockRtInfo record);

    int insertSelective(StockBlockRtInfo record);

    StockBlockRtInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockBlockRtInfo record);

    int updateByPrimaryKey(StockBlockRtInfo record);

    /**
     * 沪深两市板块分时行情数据查询，以交易时间和交易总金额降序查询，取前10条数据
     * @return
     */
    List<StockBlockRtInfo> sectorAllLimit();

    /**
     * 板块信息批量插入
     * @param list
     * @return
     */
    int insertBatch(@Param("blockInfoList") List<StockBlockRtInfo> list);
}




