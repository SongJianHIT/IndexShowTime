package tech.songjian.stock.service.impl.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.songjian.stock.mapper.StockBusinessMapper;
import tech.songjian.stock.pojo.StockBusiness;
import tech.songjian.stock.service.impl.StockService;

import java.util.List;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description
 */
@Service("stockService")
public class StockServiceImpl implements StockService {

    @Autowired
    private StockBusinessMapper stockBusinessMapper;

    @Override
    public List<StockBusiness> findAll() {
        return stockBusinessMapper.findAll();
    }
}
