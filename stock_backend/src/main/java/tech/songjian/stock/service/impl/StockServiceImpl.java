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
import tech.songjian.stock.common.domain.*;
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
import java.util.*;
import java.util.stream.Collector;
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
    public R<List<StockBlockDomain>> sectorAllLimit() {
        //1.调用 mapper 接口获取数据 TODO 优化 避免全表查询 根据时间范围查询，提高查询效率
        List<StockBlockDomain> infos = stockBlockRtInfoMapper.sectorAllLimit();
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

    /**
     * 统计国内A股大盘T日和T-1日成交量对比功能（成交量为沪市和深市成交量之和）
     * @return
     */
    @Override
    public R<Map> getStockTradeVol4Comparison() {
        // 1、获取 T 日和 T-1 日的开始时间和结束时间
        // 1.1 获取最近有效交易时间点----T日
        DateTime lastDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        // 1.2 获取对应时间的的开盘日期
        DateTime openDateTime = DateTimeUtil.getOpenDate(lastDateTime);
        // 转换成 java 中的 date
        Date startTime4T = openDateTime.toDate();
        Date endTime4T = lastDateTime.toDate();
        // TODO mock数据
        startTime4T = DateTime.parse("2021-12-27 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        endTime4T = DateTime.parse("2021-12-27 14:40:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 获取 T-1 日的区间范围
        DateTime preLastDateTime = DateTimeUtil.getPreviousTradingDay(lastDateTime);
        DateTime preOpenDateTime = DateTimeUtil.getOpenDate(preLastDateTime);
        Date startTime4PreT = preLastDateTime.toDate();
        Date endTime4PreT = preOpenDateTime.toDate();
        // TODO mock数据
        startTime4PreT = DateTime.parse("2021-12-26 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        endTime4PreT = DateTime.parse("2021-12-26 14:40:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、获取上证和深证的 market_id
        List<String> marketIds = stockInfoConfig.getInner();
        // 3、分别查询 T 日和 T-1 日的交易量数据，得到两个 List
        // 3.1 查询 T 日
        List<Map> volList = stockMarketIndexInfoMapper.getStockTradeVol(marketIds, startTime4T, endTime4T);
        if (CollectionUtils.isEmpty(volList)) {
            volList = new ArrayList<>();
        }
        // 3.2 查询 T-1 日
        List<Map> yesVolList = stockMarketIndexInfoMapper.getStockTradeVol(marketIds, startTime4PreT, endTime4PreT);
        if (CollectionUtils.isEmpty(yesVolList)) {
            yesVolList = new ArrayList<>();
        }
        // 4、组装map
        Map<String, List> map = new HashMap<>();
        map.put("volList", volList);
        map.put("yesVolList", yesVolList);
        // 5、返回数据
        return R.ok(map);
    }

    /**
     * 查询当前时间下股票的涨跌幅度区间统计功能
     * 如果当前日期不在有效时间内，则以最近的一个股票交易时间作为查询点
     * @return
     */
    @Override
    public R<Map> getStockUpDownRegion() {
        // 1、获取最近有效的交易时间点
        DateTime dateTime4Stock = DateTimeUtil.getLastDate4Stock(DateTime.now());
        Date lastDate = dateTime4Stock.toDate();
        // TODO:mock数据
        lastDate = DateTime.parse("2022-01-07 14:50:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();

        // 2、插入mapper接口获取统计数据（无序的）
        List<Map> infos = stockRtInfoMapper.getStockUpDownRegion(lastDate);
        if (CollectionUtils.isEmpty(infos)) {
            infos = new ArrayList<>();
        }
        // 保证涨幅区间按照从小到大排序，且对于没有数据的涨幅区间默认为0
        // 2.1 获取涨幅区间顺序集合
        List<String> upDownRangeList = stockInfoConfig.getUpDownRange();

        // 2.2 遍历顺序集合，在统计数据（无序）中查找对应结果，没有则设置为0
        List<Map> finalInfos = infos;
        List<Map> newList = upDownRangeList.stream().map(item->{
            Optional<Map> optional = finalInfos.stream().filter(map -> map.get("title").equals(item)).findFirst();
            Map tmp = null;
            // 判断结果是否有map
            if (optional.isPresent()) {
                tmp = optional.get();
            } else {
                tmp = new HashMap();
                tmp.put("title", item);
                tmp.put("count", 0);
            }
            return tmp;
        }).collect(Collectors.toList());

        // 3、组装数据并响应
        HashMap<String, Object> data = new HashMap<>();
        String stringDataTime = dateTime4Stock.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        data.put("time", stringDataTime);
        data.put("infos", newList);
        return R.ok(data);
    }

    /**
     * 查询个股的分时行情数据，也就是统计指定股票T日每分钟的交易数据；
     * @param stockCode
     * @return
     */
    @Override
    public R<List<Stock4MinuteDomain>> stockScreenTimeSharing(String stockCode) {
        // 1、获取最近最新时间交易时间点和对应的开盘日期
        DateTime lastDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        DateTime openDateTime = DateTimeUtil.getOpenDate(lastDateTime);
        Date endTime = lastDateTime.toDate();
        Date startTime = openDateTime.toDate();
        // TODO:mock数据
        endTime = DateTime.parse("2022-01-07 14:48:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        startTime = DateTime.parse("2022-01-07 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、调用mapper接口进行查询
        List<Stock4MinuteDomain> list = stockRtInfoMapper.getStockInfoByCodeAndDate(stockCode, startTime, endTime);
        if (CollectionUtils.isEmpty(list)) {
            list = new ArrayList<>();
        }
        return R.ok(list);
    }

    /**
     * 个股日K数据查询 ，可以根据时间区间查询数日的K线数据
     * @param code
     * @return
     */
    @Override
    public R<List<Stock4EvrDayDomain>> stockScreenDKLine(String code) {
        // 1、获取查询日期范围
        // 1.1 获取截止时间
        DateTime endDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        Date endTime = endDateTime.toDate();
        // 1.2 获取开始时间
        DateTime startDateTime = endDateTime.minusDays(10);
        Date startTime = startDateTime.toDate();
        // TODO:mock数据
        endTime = DateTime.parse("2021-12-30 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        startTime = DateTime.parse("2021-12-20 15:00:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、调用mapper接口获取查询结果
        List<Stock4EvrDayDomain> data = stockRtInfoMapper.getStockRtInfo4EvrDat(code, startTime, endTime);
        if (CollectionUtils.isEmpty(data)) {
            data = new ArrayList<>();
        }
        // 3、封装结果返回
        return R.ok(data);
    }

    /**
     * 外盘指数行情数据查询，根据时间和大盘点数降序排序取前4
     * @return
     */
    @Override
    public R<List<StockExternalIndexDomain>> getExternalIndexInfo() {
        // 1、获取国外大盘market_id
        List<String> marketIds = stockInfoConfig.getOuter();
        // 2、获取最近交易日期
        DateTime lastDate = DateTimeUtil.getLastDate4Stock(DateTime.now());
        Date date = lastDate.toDate();
        //TODO mock测试数据，后期数据通过第三方接口动态获取试试数据
        date = DateTime.parse("2022-01-02 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 3、调用 mapper 接口获取数据
        List<StockExternalIndexDomain> list = stockMarketIndexInfoMapper.getExternalIndexInfoTop4(marketIds, date);
        if (CollectionUtils.isEmpty(list)) {
            list = new ArrayList<>();
        }
        // 4、返回
        return R.ok(list);
    }

    /**
     * 根据个股代码进行模糊查询
     * @param searchStr
     * @return
     */
    @Override
    public R<List> burSearchByCode(String searchStr) {
        // 1、拼接模糊查询字符串
        searchStr = '%' + searchStr + '%';
        Date date = DateTimeUtil.getLastDate4Stock(DateTime.now()).toDate();
        // TODO mock数据
        date = DateTime.parse("2022-01-02 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、调用 mapper 查询
        List<Map> res = stockRtInfoMapper.burSearchByCode(searchStr, date);
        return R.ok(res);
    }

    /**
     * 根据股票编码查询个股主营业务
     * @param code
     * @return
     */
    @Override
    public R<StockBusinessDomain> getStockBusinessByCode(String code) {
        StockBusinessDomain stockBusinesses = stockBusinessMapper.getStockBusinessByCode(code);
        if (stockBusinesses == null) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        return R.ok(stockBusinesses);
    }
}

