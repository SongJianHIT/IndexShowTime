package tech.songjian.stock.mapper;

import org.apache.ibatis.annotations.Param;
import tech.songjian.stock.common.domain.StockBusinessDomain;
import tech.songjian.stock.pojo.StockBusiness;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Entity tech.songjian.stock.pojo.StockBusiness
 */
@Mapper
public interface StockBusinessMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StockBusiness record);

    int insertSelective(StockBusiness record);

    StockBusiness selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockBusiness record);

    int updateByPrimaryKey(StockBusiness record);

    List<StockBusiness> findAll();

    /**
     * 获取所有股票的编码
     * @return
     */
    List<String> getStockCodeList();

    /**
     * 根据股票代码查询个股业务
     * @param code
     * @return
     */
    StockBusinessDomain getStockBusinessByCode(@Param("code") String code);
}




