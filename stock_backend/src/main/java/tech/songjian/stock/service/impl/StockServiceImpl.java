package tech.songjian.stock.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.songjian.stock.common.domain.InnerMarketDomain;
import tech.songjian.stock.config.vo.StockInfoConfig;
import tech.songjian.stock.mapper.StockBlockRtInfoMapper;
import tech.songjian.stock.mapper.StockBusinessMapper;
import tech.songjian.stock.mapper.StockMarketIndexInfoMapper;
import tech.songjian.stock.pojo.StockBlockRtInfo;
import tech.songjian.stock.pojo.StockBusiness;
import tech.songjian.stock.service.StockService;
import tech.songjian.stock.utils.DateTimeUtil;
import tech.songjian.stock.vo.resp.R;
import tech.songjian.stock.vo.resp.ResponseCode;

import java.util.Date;
import java.util.List;

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
}
