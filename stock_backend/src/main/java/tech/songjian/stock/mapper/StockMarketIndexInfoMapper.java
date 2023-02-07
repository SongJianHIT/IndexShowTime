package com.itheima.stock.mapper;

import com.itheima.stock.pojo.StockMarketIndexInfo;

/**
 * @Entity com.itheima.stock.pojo.StockMarketIndexInfo
 */
public interface StockMarketIndexInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockMarketIndexInfo record);

    int insertSelective(StockMarketIndexInfo record);

    StockMarketIndexInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockMarketIndexInfo record);

    int updateByPrimaryKey(StockMarketIndexInfo record);

}




