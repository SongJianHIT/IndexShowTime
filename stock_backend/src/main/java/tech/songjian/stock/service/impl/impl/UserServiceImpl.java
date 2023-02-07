package tech.songjian.stock.service.impl.impl;

import com.google.common.base.Strings;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.songjian.stock.mapper.SysUserMapper;
import tech.songjian.stock.pojo.SysUser;
import tech.songjian.stock.service.impl.UserService;
import tech.songjian.stock.utils.IdWorker;
import tech.songjian.stock.vo.req.LoginReqVo;
import tech.songjian.stock.vo.resp.LoginRespVo;
import tech.songjian.stock.vo.resp.R;
import tech.songjian.stock.vo.resp.ResponseCode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public R<LoginRespVo> login(LoginReqVo vo) {
        // 1.判断vo是否存在 或者 用户名是否存在 或者 密码是否存在 或者 验证码是否存在 或者 rkey 是否存在
        if (vo == null || Strings.isNullOrEmpty(vo.getUsername()) || Strings.isNullOrEmpty(vo.getPassword())
            || Strings.isNullOrEmpty(vo.getCode()) || Strings.isNullOrEmpty(vo.getRkey())) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }

        // 1.1 获取校验验证码
        String redisCode = (String) redisTemplate.opsForValue().get(vo.getRkey());
        // 1.2 校验
        if (redisCode == null || !redisCode.equals(vo.getCode())) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }
        // 1.3 redis删除key
        redisTemplate.delete(vo.getRkey());

        // 2.根据用户名用户是否存在
        SysUser userInfo= sysUserMapper.findUserInfoByUserName(vo.getUsername());
        if (userInfo==null) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }

        // 3.判断密码,不匹配
        if (!passwordEncoder.matches(vo.getPassword(),userInfo.getPassword())) {
            return R.error(ResponseCode.SYSTEM_PASSWORD_ERROR.getMessage());
        }

        // 4.属性赋值 两个类之间属性名称一致
        LoginRespVo respVo = new LoginRespVo();
        BeanUtils.copyProperties(userInfo,respVo);

        return R.ok(respVo);
    }

    @Override
    public R<Map> genCapchaCode() {
        // 1、生成随机校验码，长度为 4
        String checkCode = RandomStringUtils.randomNumeric(4);
        // 2、生成类似与 sessionID 的 id 作为 key，保存在 redis，设置有效期 60s
        long rkey = idWorker.nextId();
        // 建议：往 redis 中保存的数据以 String 格式为主！
        String sessionId = String.valueOf(rkey);
        redisTemplate.opsForValue().set(sessionId, checkCode, 60, TimeUnit.SECONDS);
        // 3、组装响应的 map 对象
        Map<String, String> map = new HashMap<>();
        map.put("code", checkCode);
        map.put("rkey", sessionId);
        // 4、返回数据
        return R.ok(map);
    }
}
