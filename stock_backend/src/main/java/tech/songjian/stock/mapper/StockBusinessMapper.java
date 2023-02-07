package com.itheima.stock.mapper;

import com.itheima.stock.pojo.StockBusiness;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Entity com.itheima.stock.pojo.StockBusiness
 */
@Mapper
public interface StockBusinessMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockBusiness record);

    int insertSelective(StockBusiness record);

    StockBusiness selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockBusiness record);

    int updateByPrimaryKey(StockBusiness record);

    List<StockBusiness> findAll();

}




