package tech.songjian.stock.controller;


import com.sun.deploy.net.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.songjian.stock.common.domain.InnerMarketDomain;
import tech.songjian.stock.common.domain.StockUpdownDomain;
import tech.songjian.stock.pojo.StockBlockRtInfo;
import tech.songjian.stock.pojo.StockBusiness;
import tech.songjian.stock.service.StockService;
import tech.songjian.stock.vo.resp.PageResult;
import tech.songjian.stock.vo.resp.R;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    /**
     * 查询板块信息
     * 沪深两市板块分时行情数据查询，以交易时间和交易总金额降序查询，取前10条数据
     * @return
     */
    @GetMapping("/sector/all")
    public R<List<StockBlockRtInfo>> sectorAll() {
        return stockService.sectorAllLimit();
    }

    /**
     * 统计沪深两个城市的最新交易数据
     * 并按涨幅降序排序，查询前十条数据
     * @return
     */
    @GetMapping("/stock/increase")
    public R<List<StockUpdownDomain>> getStockRtInfoLimit() {
        return stockService.getStockRtInfoLimit();
    }

    /**
     * 查询沪深两市的全部涨幅榜数据
     * 按照时间顺序和涨幅分页查询
     * @param page 当前页
     * @param pageSize 当前页大小
     * @return
     */
    @GetMapping("/stock/all")
    public R<PageResult<StockUpdownDomain>> getStockRtInfo4Page(Integer page, Integer pageSize) {
        return stockService.getStockRtInfo4Page(page, pageSize);
    }

    /**
     * 统计T日（最近一次股票交易日）的涨停跌停的分时统计
     * @return
     */
    @GetMapping("/stock/updown/count")
    public R<Map> getStockUpdownCount() {
        return stockService.getStockUpdownCount();
    }

    /**
     * 导出股票信息到excel下
     * @param response http的响应对象，可获取写出流对象
     * @param page 当前页
     * @param pageSize 每页大小
     */
    @GetMapping("/stock/export")
    public void stockExport(HttpServletResponse response, Integer page, Integer pageSize) throws IOException {
        stockService.stockExport(response, page, pageSize);
    }
}
