/**
 * @projectName stock_parent
 * @package tech.songjian.stock.service.impl
 * @className tech.songjian.stock.service.impl.StockTimerTaskServiceImpl
 */
package tech.songjian.stock.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import tech.songjian.stock.config.vo.StockInfoConfig;
import tech.songjian.stock.mapper.StockBlockRtInfoMapper;
import tech.songjian.stock.mapper.StockBusinessMapper;
import tech.songjian.stock.mapper.StockMarketIndexInfoMapper;
import tech.songjian.stock.mapper.StockRtInfoMapper;
import tech.songjian.stock.pojo.StockBlockRtInfo;
import tech.songjian.stock.pojo.StockMarketIndexInfo;
import tech.songjian.stock.pojo.StockRtInfo;
import tech.songjian.stock.service.StockTimerTaskService;
import tech.songjian.stock.utils.DateTimeUtil;
import tech.songjian.stock.utils.IdWorker;
import tech.songjian.stock.utils.ParserStockInfoUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * StockTimerTaskServiceImpl
 * @description 实现类
 * @author SongJian
 * @date 2023/2/11 16:11
 * @version
 */
@Service("StockTimerTaskService")
@Slf4j
public class StockTimerTaskServiceImpl implements StockTimerTaskService {

    @Autowired
    private StockInfoConfig stockInfoConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private StockMarketIndexInfoMapper stockMarketIndexInfoMapper;

    @Autowired
    private StockBusinessMapper stockBusinessMapper;

    @Autowired
    private ParserStockInfoUtil parserStockInfoUtil;

    @Autowired
    private StockRtInfoMapper stockRtInfoMapper;

    @Autowired
    private StockBlockRtInfoMapper stockBlockRtInfoMapper;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 获取国内大盘的实时数据信息
     */
    @Override
    public void collectInnerMarketInfo() {
        // 1、定义采集的url接口，生成完整的url地址
        String marketUrl = stockInfoConfig.getMarketUrl() + String.join(",", stockInfoConfig.getInner());
        // 2、调用 restTemplate 采集数据
        // 2.1、组装请求头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer", "https://finance.sina.com.cn/stock/");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        // 2.2、组装请求对象
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        // 2.3、发起请求
        String result = restTemplate.postForObject(marketUrl, entity, String.class);
        // log.info("当前采集的数据: {}", result);
        // 3、数据解析
        // 3.1、编写正则表达式
        // var hq_str_s_sh000001="上证指数,3260.6734,-9.7092,-0.30,2606269,34174987";
        // var hq_str_s_sz399001="深证成指,11976.85,-71.419,-0.59,458163546,55112605"
        String reg="var hq_str_(.+)=\"(.+)\";";
        // 3.2、正则匹配
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(result);
        // 3.3、收集大盘封装后后的对象
        List<StockMarketIndexInfo> list = new ArrayList<>();
        while (matcher.find()) {
            // 获取大盘的id
            String marketCode = matcher.group(1);
            // 其它信息
            String other = matcher.group(2);
            String[] others = other.split(",");
            // 大盘名称
            String marketName = others[0];
            // 当前点
            BigDecimal curPoint = new BigDecimal(others[1]);
            // 当前价格
            BigDecimal curPrice = new BigDecimal(others[2]);
            // 涨跌率
            BigDecimal upDownRate = new BigDecimal(others[3]);
            // 成交量
            Long tradeAmount = Long.valueOf(others[4]);
            // 成交金额
            Long tradeVol = Long.valueOf(others[5]);
            // 当前日期
            Date now = DateTimeUtil.getDateTimeWithoutSecond(DateTime.now()).toDate();

            //封装对象
            StockMarketIndexInfo stockMarketIndexInfo = StockMarketIndexInfo.builder()
                    .id(idWorker.nextId()+"")
                    .markName(marketName)
                    .tradeVolume(tradeVol)
                    .tradeAccount(tradeAmount)
                    .updownRate(upDownRate)
                    .curTime(now)
                    .curPoint(curPoint)
                    .currentPrice(curPrice)
                    .markId(marketCode)
                    .build();

            // 添加集合
            list.add(stockMarketIndexInfo);
        }
        log.info("集合长度：{}，内容：{}", list.size(), list);
        if (CollectionUtils.isEmpty(list)) {
            log.info("");
            return;
        }
        String curTime = DateTime.now().toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));
        log.info("采集的大盘数据：{},当前时间：{}",list,curTime);

//        int count = stockMarketIndexInfoMapper.insertBatch(list);
//        log.info("批量插入 {} 条大盘数据", count);
    }


    /**
     * 采集国内 A 股 股票详情信息
     */
    @Override
    public void collectAShareInfo() {
        // 1、获取所有股票code集合（3700+）
        List<String> stockCodeList = stockBusinessMapper.getStockCodeList();
        // 转化集合中股票编码，添加前缀
        stockCodeList = stockCodeList.stream().map(id -> {
            return id.startsWith("6") ? "sh" + id : "sz" + id;
        }).collect(Collectors.toList());
        // 设置请求头对象
        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer","https://finance.sina.com.cn/stock/");
        headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // 2、将股票code集合分片处理，进行均等分
        /**  单线程串行
        Lists.partition(stockCodeList,20).forEach(list->{
            //拼接股票url地址
            String stockUrl=stockInfoConfig.getMarketUrl()+String.join(",",list);
            //获取响应数据
            String result = restTemplate.postForObject(stockUrl, entity, String.class);
            // 解析处理, 3:表示A股股票
            List<StockRtInfo> infos = parserStockInfoUtil.parser4StockOrMarketInfo(result, 3);
            log.info("数据量：{}",infos.size());

            // 批量插入
            int inserts = stockRtInfoMapper.insertBatch(infos);
            if (inserts > 0) {
                log.info("插入股票详细数据 {} 条", inserts);
            }
        });*/
        // 多线程，异步执行
        Lists.partition(stockCodeList,20).forEach(list->{
            // 加入线程池后，异步多线程处理数据采集
            // 提高操作效率，但数据库IO会增加
            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    //拼接股票url地址
                    String stockUrl=stockInfoConfig.getMarketUrl()+String.join(",",list);
                    //获取响应数据
                    String result = restTemplate.postForObject(stockUrl, entity, String.class);
                    // 解析处理, 3:表示A股股票
                    List<StockRtInfo> infos = parserStockInfoUtil.parser4StockOrMarketInfo(result, 3);
                    log.info(infos.toString());

                    // 批量插入
//                    int inserts = stockRtInfoMapper.insertBatch(infos);
//                    if (inserts > 0) {
//                        log.info("插入股票详细数据 {} 条", inserts);
//                    }
                }
            });
        });
    }

    /**
     * 获取板块数据
     */
    @Override
    public void getStockSectorRtIndex() {
        // 1、发送板块数据请求
        String result = restTemplate.getForObject(stockInfoConfig.getBlockUrl(), String.class);
        // 2、将响应数据转化为集合数据
        List<StockBlockRtInfo> infos = parserStockInfoUtil.parse4StockBlock(result);
        log.info("获取板块数据 {} 条", infos.size());
        // 3、批量保存集合数据
//        int insert = stockBlockRtInfoMapper.insertBatch(infos);
//        if (insert > 0) {
//            log.info("插入板块数据 {} 条", insert);
//        }
    }
}

