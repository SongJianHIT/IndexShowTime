package tech.songjian.stock.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.songjian.stock.common.domain.InnerMarketDomain;
import tech.songjian.stock.pojo.StockBusiness;
import tech.songjian.stock.service.StockService;
import tech.songjian.stock.vo.resp.R;

import java.util.List;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description
 */
@RestController
@RequestMapping("/api/quot")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("/stock/business/all")
    public List<StockBusiness> findAllBusinessInfo(){
       return stockService.findAll();
    }

    /**
     * 获取最新的 A 股大盘信息
     * 如果当前不在股票交易日或交易时间，则显示最近最新的交易数据信息
     * @return
     */
    @GetMapping("/index/all")
    public R<List<InnerMarketDomain>> getNewAMarketInfo() {
        return stockService.getNewAMarketInfo();
    }
}
