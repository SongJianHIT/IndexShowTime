package com.itheima.stock.mapper;

import com.itheima.stock.pojo.StockMarketLogPrice;

/**
 * @Entity com.itheima.stock.pojo.StockMarketLogPrice
 */
public interface StockMarketLogPriceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockMarketLogPrice record);

    int insertSelective(StockMarketLogPrice record);

    StockMarketLogPrice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockMarketLogPrice record);

    int updateByPrimaryKey(StockMarketLogPrice record);

}




