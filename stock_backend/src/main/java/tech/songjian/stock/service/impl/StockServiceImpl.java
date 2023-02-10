package tech.songjian.stock.service.impl;

import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.sun.deploy.net.HttpResponse;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.songjian.stock.common.domain.InnerMarketDomain;
import tech.songjian.stock.common.domain.StockExcelDomain;
import tech.songjian.stock.common.domain.StockUpdownDomain;
import tech.songjian.stock.config.vo.StockInfoConfig;
import tech.songjian.stock.mapper.StockBlockRtInfoMapper;
import tech.songjian.stock.mapper.StockBusinessMapper;
import tech.songjian.stock.mapper.StockMarketIndexInfoMapper;
import tech.songjian.stock.mapper.StockRtInfoMapper;
import tech.songjian.stock.pojo.StockBlockRtInfo;
import tech.songjian.stock.pojo.StockBusiness;
import tech.songjian.stock.service.StockService;
import tech.songjian.stock.utils.DateTimeUtil;
import tech.songjian.stock.vo.resp.PageResult;
import tech.songjian.stock.vo.resp.R;
import tech.songjian.stock.vo.resp.ResponseCode;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author by songjian
 * @Date 2022/3/13
 * @Description
 */
@Service("stockService")
public class StockServiceImpl implements StockService {

    @Autowired
    private StockBusinessMapper stockBusinessMapper;

    @Autowired
    private StockMarketIndexInfoMapper stockMarketIndexInfoMapper;

    @Autowired
    private StockInfoConfig stockInfoConfig;

    @Autowired
    private StockBlockRtInfoMapper stockBlockRtInfoMapper;

    @Autowired
    private StockRtInfoMapper stockRtInfoMapper;

    @Override
    public List<StockBusiness> findAll() {
        return stockBusinessMapper.findAll();
    }


    /**
     * 获取最新的 A 股大盘信息
     * 如果当前不在股票交易日或交易时间，则显示最近最新的交易数据信息
     * @return
     */
    @Override
    public R<List<InnerMarketDomain>> getNewAMarketInfo() {
        // 1、获取国内 A 股大盘 id 集合
        List<String> inners = stockInfoConfig.getInner();

        // 2、获取最近股票交易日期
        DateTime lastDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());

        // 3、转java中的Date
        Date date = lastDateTime.toDate();
        //TODO mock测试数据，后期数据通过第三方接口动态获取试试数据
        date = DateTime.parse("2022-01-03 11:15:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 4、将获取的 java Date 传入接口
        List<InnerMarketDomain> list = stockMarketIndexInfoMapper.getMarketInfo(inners, date);

        // 5、返回查询结果
        return R.ok(list);
    }

    /**
     * 查询板块信息
     * 沪深两市板块分时行情数据查询，以交易时间和交易总金额降序查询，取前10条数据
     * @return
     */
    @Override
    public R<List<StockBlockRtInfo>> sectorAllLimit() {
        //1.调用 mapper 接口获取数据 TODO 优化 避免全表查询 根据时间范围查询，提高查询效率
        List<StockBlockRtInfo> infos = stockBlockRtInfoMapper.sectorAllLimit();
        //2.组装数据
        if (CollectionUtils.isEmpty(infos)) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        return R.ok(infos);
    }

    /**
     * 统计沪深两个城市的最新交易数据
     * 并按涨幅降序排序，查询前十条数据
     * @return
     */
    @Override
    public R<List<StockUpdownDomain>> getStockRtInfoLimit() {
        // 1、获取最近最新股票有效交易时间点，精确到分钟
        Date lastDate = DateTimeUtil.getLastDate4Stock(DateTime.now()).toDate();
        // TODO mock 数据
        lastDate = DateTime.parse("2021-12-30 09:32:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、调用mapper进行查询
        List<StockUpdownDomain> list = stockRtInfoMapper.getStockRtInfoLimit(lastDate);
        if (CollectionUtils.isEmpty(list)) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        return R.ok(list);
    }

    /**
     * 查询沪深两市的全部涨幅榜数据
     * 按照时间顺序和涨幅分页查询
     * @param page 当前页
     * @param pageSize 当前页大小
     * @return
     */
    @Override
    public R<PageResult<StockUpdownDomain>> getStockRtInfo4Page(Integer page, Integer pageSize) {
        // 1、设置分页参数
        PageHelper.startPage(page, pageSize);
        // 2、查询
        List<StockUpdownDomain> pages = stockRtInfoMapper.getStockRtInfo4All();
        if (CollectionUtils.isEmpty(pages)) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        // 3、组装 pageInfo 对象，他封装了一切的分页信息
        PageInfo<StockUpdownDomain> pageInfo = new PageInfo<>(pages);
        PageResult<StockUpdownDomain> pageResult = new PageResult<>(pageInfo);
        return R.ok(pageResult);
    }

    /**
     * 统计T日（最近一次股票交易日）的涨停跌停的分时统计
     * @return
     */
    @Override
    public R<Map> getStockUpdownCount() {
        // 1、获取最近的股票交易日的开盘和收盘时间
        // 1.1 获取最近的交易时间
        DateTime avaliableTimePoint = DateTimeUtil.getLastDate4Stock(DateTime.now());
        // 1.2 根据有效的时间点，获取对应日期的开盘和收盘日期
        Date openTime = DateTimeUtil.getOpenDate(avaliableTimePoint).toDate();
        Date closeTime = DateTimeUtil.getCloseDate(avaliableTimePoint).toDate();
        // TODO mock数据
        openTime = DateTime.parse("2022-01-07 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        closeTime = DateTime.parse("2022-01-07 15:00:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();

        // 2、查询涨停的统计数据
        List<Map> upList = stockRtInfoMapper.getStockUpdownCount(openTime, closeTime, 1);
        // 3、查询跌停的统计数据
        List<Map> downList = stockRtInfoMapper.getStockUpdownCount(openTime, closeTime, 0);
        // 4、组装 map，将涨停数据和跌停数据组装
        HashMap<String, List> map = new HashMap<>();
        map.put("upList", upList);
        map.put("downList", downList);
        // 5、返回结果
        return R.ok(map);
    }

    /**
     * 导出股票信息到excel下
     * @param response http的响应对象，可获取写出流对象
     * @param page 当前页
     * @param pageSize 每页大小
     */
    @Override
    public void stockExport(HttpServletResponse response, Integer page, Integer pageSize) throws IOException {
        // 1、设置响应数据类型：excel
        response.setContentType("application/vnd.ms-excel");
        // 2、设置响应数据编码格式
        response.setCharacterEncoding("utf-8");
        // 3、设置默认的文件名称
        // 此处的 URLEncoder.encode 可以防止中文乱码，与 easyExcel 无关
        String fileName = URLEncoder.encode("stockRt", "UTF-8");
        // 设置默认文件名称
        response.setHeader("content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 读取导出的数据集合
        // 1、设置分页参数
        PageHelper.startPage(page, pageSize);
        // 2、查询
        List<StockUpdownDomain> pages = stockRtInfoMapper.getStockRtInfo4All();
        if (CollectionUtils.isEmpty(pages)) {
            R<String> error = R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
            // 将错误信息转化为 json 格式字符串响应前段
            String jsonData = new Gson().toJson(error);
            response.getWriter().write(jsonData);
            return;
        }

        // 将 List<StockUpdownDomain> 转化为 List<ExcelDomain>
        List<StockExcelDomain> domains = pages.stream().map(item -> {
            StockExcelDomain stockExcelDomain = new StockExcelDomain();
            BeanUtils.copyProperties(item, stockExcelDomain);
            return stockExcelDomain;
        }).collect(Collectors.toList());

        // 数据导出
        EasyExcel
                .write(response.getOutputStream(), StockExcelDomain.class)
                .sheet("stockInfo")
                .doWrite(domains);
    }
}

