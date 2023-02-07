package com.itheima.stock.mapper;

import com.itheima.stock.pojo.StockBlockRtInfo;

/**
 * @Entity com.itheima.stock.pojo.StockBlockRtInfo
 */
public interface StockBlockRtInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockBlockRtInfo record);

    int insertSelective(StockBlockRtInfo record);

    StockBlockRtInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockBlockRtInfo record);

    int updateByPrimaryKey(StockBlockRtInfo record);

}




