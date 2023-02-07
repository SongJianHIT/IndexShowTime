package com.itheima.stock.service;

import com.itheima.stock.pojo.StockBusiness;

import java.util.List;

/**
 * @author by itheima
 * @Date 2022/3/13 股票相关服务接口
 * @Description
 */
public interface StockService {

    /**
     * 查询所有主营业务信息
     * @return
     */
    List<StockBusiness> findAll();


}
