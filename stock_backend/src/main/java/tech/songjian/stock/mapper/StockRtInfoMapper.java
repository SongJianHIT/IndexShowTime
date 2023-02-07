package com.itheima.stock.mapper;

import com.itheima.stock.pojo.StockRtInfo;

/**
 * @Entity com.itheima.stock.pojo.StockRtInfo
 */
public interface StockRtInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockRtInfo record);

    int insertSelective(StockRtInfo record);

    StockRtInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockRtInfo record);

    int updateByPrimaryKey(StockRtInfo record);

}




