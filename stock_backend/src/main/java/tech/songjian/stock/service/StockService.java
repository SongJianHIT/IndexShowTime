package tech.songjian.stock.service;

import com.sun.deploy.net.HttpResponse;
import tech.songjian.stock.common.domain.*;
import tech.songjian.stock.pojo.StockBlockRtInfo;
import tech.songjian.stock.pojo.StockBusiness;
import tech.songjian.stock.vo.resp.PageResult;
import tech.songjian.stock.vo.resp.R;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    /**
     * 查询板块信息
     * 沪深两市板块分时行情数据查询，以交易时间和交易总金额降序查询，取前10条数据
     * @return
     */
    R<List<StockBlockDomain>> sectorAllLimit();

    /**
     * 统计沪深两个城市的最新交易数据
     * 并按涨幅降序排序，查询前十条数据
     * @return
     */
    R<List<StockUpdownDomain>> getStockRtInfoLimit();

    /**
     * 查询沪深两市的全部涨幅榜数据
     * 按照时间顺序和涨幅分页查询
     * @param page 当前页
     * @param pageSize 当前页大小
     * @return
     */
    R<PageResult<StockUpdownDomain>> getStockRtInfo4Page(Integer page, Integer pageSize);

    /**
     * 统计T日（最近一次股票交易日）的涨停跌停的分时统计
     * @return
     */
    R<Map> getStockUpdownCount();

    /**
     * 导出股票信息到excel下
     * @param response http的响应对象，可获取写出流对象
     * @param page 当前页
     * @param pageSize 每页大小
     */
    void stockExport(HttpServletResponse response, Integer page, Integer pageSize) throws IOException;


    /**
     * 统计国内A股大盘T日和T-1日成交量对比功能（成交量为沪市和深市成交量之和）
     * @return
     */
    R<Map> getStockTradeVol4Comparison();

    /**
     * 查询当前时间下股票的涨跌幅度区间统计功能
     * 如果当前日期不在有效时间内，则以最近的一个股票交易时间作为查询点
     * @return
     */
    R<Map> getStockUpDownRegion();

    /**
     * 查询个股的分时行情数据，也就是统计指定股票T日每分钟的交易数据；
     * @param stockCode
     * @return
     */
    R<List<Stock4MinuteDomain>> stockScreenTimeSharing(String stockCode);

    /**
     * 个股日K数据查询 ，可以根据时间区间查询数日的K线数据
     * @param code
     * @return
     */
    R<List<Stock4EvrDayDomain>> stockScreenDKLine(String code);

    /**
     * 外盘指数行情数据查询，根据时间和大盘点数降序排序取前4
     * @return
     */
    R<List<StockExternalIndexDomain>> getExternalIndexInfo();

    /**
     * 根据个股代码进行模糊查询
     * @param searchStr
     * @return
     */
    R<List> burSearchByCode(String searchStr);

    /**
     * 根据股票编码查询个股主营业务
     * @param code
     * @return
     */
    R<StockBusinessDomain> getStockBusinessByCode(String code);

    /**
     * 个股周K线展示
     * @param code
     * @return
     */
    R<List<WeeklineDomain>> getRtStockWeekline(String code);

    /**
     * 获取个股最新分时行情数据，主要包含：
     * 开盘价、前收盘价、最新价、最高价、最低价、成交金额和成交量、交易时间信息;
     * @return
     */
    R<StockDetailSecDomain> getStockDetailsByCode(String code);
}
