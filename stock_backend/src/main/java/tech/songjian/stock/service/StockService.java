package tech.songjian.stock.service;

import com.sun.deploy.net.HttpResponse;
import tech.songjian.stock.common.domain.InnerMarketDomain;
import tech.songjian.stock.common.domain.StockUpdownDomain;
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
    R<List<StockBlockRtInfo>> sectorAllLimit();

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
}
