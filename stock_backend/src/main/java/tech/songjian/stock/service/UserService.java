package tech.songjian.stock.service;

import tech.songjian.stock.pojo.SysUser;
import tech.songjian.stock.vo.req.ConditionalQueryUserReq;
import tech.songjian.stock.vo.req.LoginReqVo;
import tech.songjian.stock.vo.resp.ConditionQueryUserResp;
import tech.songjian.stock.vo.resp.LoginRespVo;
import tech.songjian.stock.vo.resp.NewLoginReqVo;
import tech.songjian.stock.vo.resp.R;

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
    R<NewLoginReqVo> login(LoginReqVo vo);

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
}
