package tech.songjian.stock.mapper;

import tech.songjian.stock.pojo.StockMarketLogPrice;

/**
 * @Entity tech.songjian.stock.pojo.StockMarketLogPrice
 */
public interface StockMarketLogPriceMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockMarketLogPrice record);

    int insertSelective(StockMarketLogPrice record);

    StockMarketLogPrice selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockMarketLogPrice record);

    int updateByPrimaryKey(StockMarketLogPrice record);

}




