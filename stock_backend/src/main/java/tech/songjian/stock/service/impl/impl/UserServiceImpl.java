package tech.songjian.stock.service.impl.impl;

import com.google.common.base.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.songjian.stock.mapper.SysUserMapper;
import tech.songjian.stock.pojo.SysUser;
import tech.songjian.stock.service.impl.UserService;
import tech.songjian.stock.vo.req.LoginReqVo;
import tech.songjian.stock.vo.resp.LoginRespVo;
import tech.songjian.stock.vo.resp.R;
import tech.songjian.stock.vo.resp.ResponseCode;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description 用户服务实现
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public R<LoginRespVo> login(LoginReqVo vo) {
        //1.判断vo是否存在 或者 用户名是否存在 或者 密码是否存在
        if (vo==null || Strings.isNullOrEmpty(vo.getUsername()) || Strings.isNullOrEmpty(vo.getPassword())) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }
        //2.根据用户名用户是否存在
        SysUser userInfo= sysUserMapper.findUserInfoByUserName(vo.getUsername());
        if (userInfo==null) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }
        //3.判断密码,不匹配
        if (!passwordEncoder.matches(vo.getPassword(),userInfo.getPassword())) {
            return R.error(ResponseCode.SYSTEM_PASSWORD_ERROR.getMessage());
        }
        //4.属性赋值 两个类之间属性名称一致
        LoginRespVo respVo = new LoginRespVo();
        BeanUtils.copyProperties(userInfo,respVo);

        return R.ok(respVo);
    }
}
