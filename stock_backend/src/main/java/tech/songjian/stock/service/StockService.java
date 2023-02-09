package tech.songjian.stock.service;

import tech.songjian.stock.common.domain.InnerMarketDomain;
import tech.songjian.stock.pojo.StockBusiness;
import tech.songjian.stock.vo.resp.R;

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

    /**
     * 获取最新的 A 股大盘信息
     * 如果当前不在股票交易日或交易时间，则显示最近最新的交易数据信息
     * @return
     */
    R<List<InnerMarketDomain>> getNewAMarketInfo();
}