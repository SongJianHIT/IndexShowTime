package tech.songjian.stock.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.songjian.stock.common.domain.*;
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
    public R<List<StockBlockDomain>> sectorAll() {
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

    /**
     * 统计国内A股大盘T日和T-1日成交量对比功能（成交量为沪市和深市成交量之和）
     * @return
     */
    @GetMapping("/stock/tradevol")
    public R<Map> getStockTradeVol4Comparison() {
        return stockService.getStockTradeVol4Comparison();
    }

    /**
     * 查询当前时间下股票的涨跌幅度区间统计功能
     * 如果当前日期不在有效时间内，则以最近的一个股票交易时间作为查询点
     * @return
     */
    @GetMapping("/stock/updown")
    public R<Map> getStockUpDownRegion(){
        return stockService.getStockUpDownRegion();
    }

    /**
     * 查询个股的分时行情数据，也就是统计指定股票T日每分钟的交易数据；
     *
     * @param stockCode 股票编码
     * @return
     */
    @GetMapping("/stock/screen/time-sharing")
    public R<List<Stock4MinuteDomain>> stockScreenTimeSharing(@RequestParam("code") String stockCode) {
        return stockService.stockScreenTimeSharing(stockCode);
    }

    /**
     * 个股日K数据查询 ，可以根据时间区间查询数日的K线数据
     * @param code
     * @return
     */
    @GetMapping("/stock/screen/dkline")
    public R<List<Stock4EvrDayDomain>> stockScreenDKLine(String code) {
        return stockService.stockScreenDKLine(code);
    }

    /**
     * 外盘指数行情数据查询，根据时间和大盘点数降序排序取前4
     * @return
     */
    @GetMapping("/external/index")
    public R<List<StockExternalIndexDomain>> getExternalIndexInfo() {
        return stockService.getExternalIndexInfo();
    }

    /**
     * 根据个股代码进行模糊查询
     * @param searchStr
     * @return
     */
    @GetMapping("/stock/search")
    public R<List> burSearchByCode(String searchStr) {
        return stockService.burSearchByCode(searchStr);
    }

    /**
     * 根据股票编码查询个股主营业务
     * @param code
     * @return
     */
    @GetMapping("/stock/describe")
    public R<StockBusinessDomain> getStockBusinessByCode(String code) {
        return stockService.getStockBusinessByCode(code);
    }

    /**
     * 个股周K线展示
     * @param code
     * @return
     */
    @GetMapping("/stock/screen/weekkline")
    public R<List<WeeklineDomain>> getRtStockWeekline(String code){
        return stockService.getRtStockWeekline(code);
    }
}
