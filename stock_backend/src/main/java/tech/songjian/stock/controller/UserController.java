package tech.songjian.stock.controller;

import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.songjian.stock.pojo.SysUser;
import tech.songjian.stock.service.UserService;
import tech.songjian.stock.vo.req.ConditionalQueryUserReq;
import tech.songjian.stock.vo.req.LoginReqVo;
import tech.songjian.stock.vo.resp.ConditionQueryUserResp;
import tech.songjian.stock.vo.resp.NewLoginReqVo;
import tech.songjian.stock.vo.resp.R;

import java.util.Map;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description
 */
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 当前用户登录后，仅仅加载了用户表相关信息，接下来完成的功能是完善用户权限相关的信息；
     * @param vo
     * @return
     */
    @PostMapping("/login")
    public R<NewLoginReqVo> login(@RequestBody LoginReqVo vo){
        return userService.login(vo);
    }

    /**
     * 生成验证码
     *  map结构：
     *      code： xxx,
     *      rkey: xxx
     * @return
     */
    @GetMapping("/captcha")
    public R<Map> genCapchaCode() {
        return userService.genCapchaCode();
    }

    /**
     * 多条件查询用户信息
     * @param req
     * @return
     */
    @PostMapping("/users")
    public R<ConditionQueryUserResp> conditionQueryUser(@RequestBody ConditionalQueryUserReq req) {
        return userService.conditionQueryUser(req);
    }

    /**
     * 添加用户信息
     * @param adduser
     * @return
     */
    @PostMapping("/user")
    public R addUsers(@RequestBody SysUser adduser){
        return userService.addUsers(adduser);
    }
}
