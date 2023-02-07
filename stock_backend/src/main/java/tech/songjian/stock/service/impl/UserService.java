package com.itheima.stock.service;

import com.itheima.stock.vo.req.LoginReqVo;
import com.itheima.stock.vo.resp.LoginRespVo;
import com.itheima.stock.vo.resp.R;

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
    R<LoginRespVo> login(LoginReqVo vo);
}
