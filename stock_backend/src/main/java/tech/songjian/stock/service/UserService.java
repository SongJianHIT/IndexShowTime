package tech.songjian.stock.service;

import tech.songjian.stock.pojo.SysUser;
import tech.songjian.stock.vo.req.ConditionalQueryUserReq;
import tech.songjian.stock.vo.req.LoginReqVo;
import tech.songjian.stock.vo.req.SetUserInfoVo;
import tech.songjian.stock.vo.resp.*;

import java.util.List;
import java.util.Map;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description 定义用户服务接口
 */
public interface UserService {
    /**
     * 用户登录功能
     * @param vo
     * @return
     */
    void login(LoginReqVo vo);

    /**
     * 生成验证码
     * @return
     */
    R<Map> genCapchaCode();

    /**
     * 多条件查询用户信息
     * @param req
     * @return
     */
    R<ConditionQueryUserResp> conditionQueryUser(ConditionalQueryUserReq req);

    /**
     * 添加用户信息
     * @param adduser
     * @return
     */
    R addUsers(SysUser adduser);


    /**
     * 批量删除用户信息，delete请求可通过请求体携带数据
     * @param userIds
     * @return
     */
    R<String> deleteByUserId(List<Long> userIds);

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    R<GetUserInfoVo> getUserInfoById(Long userId);

    /**
     * 更新用户信息
     * @param setUserInfoVo
     * @return
     */
    R<String> updateUserInfo(SetUserInfoVo setUserInfoVo);
}
