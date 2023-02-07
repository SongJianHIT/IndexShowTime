package tech.songjian.stock.mapper;

import tech.songjian.stock.pojo.SysLog;

/**
 * @Entity tech.songjian.stock.pojo.SysLog
 */
public interface SysLogMapper {

    int deleteByPrimaryKey(Long id);

    int insert(SysLog record);

    int insertSelective(SysLog record);

    SysLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SysLog record);

    int updateByPrimaryKey(SysLog record);

}




