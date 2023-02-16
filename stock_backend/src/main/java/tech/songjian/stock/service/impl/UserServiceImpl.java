package tech.songjian.stock.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.songjian.stock.common.domain.PermissionDomain;
import tech.songjian.stock.mapper.SysUserMapper;
import tech.songjian.stock.pojo.SysPermission;
import tech.songjian.stock.pojo.SysUser;
import tech.songjian.stock.service.UserService;
import tech.songjian.stock.utils.IdWorker;
import tech.songjian.stock.vo.req.ConditionalQueryUserReq;
import tech.songjian.stock.vo.req.LoginReqVo;
import tech.songjian.stock.vo.req.SetUserInfoVo;
import tech.songjian.stock.vo.resp.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description 用户服务实现
 */
@Service("userService")
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 用户登录功能实现
     * 功能描述：当前用户登录后，仅仅加载了用户表相关信息，接下来完成的功能是完善用户权限相关的信息；
     * @param vo
     * @return
     */
    @Override
    public void login(LoginReqVo vo) {
//        // 1.判断vo是否存在 或者 用户名是否存在 或者 密码是否存在 或者 验证码是否存在 或者 rkey 是否存在
//        if (vo == null || Strings.isNullOrEmpty(vo.getUsername()) || Strings.isNullOrEmpty(vo.getPassword())
//            || Strings.isNullOrEmpty(vo.getCode()) || Strings.isNullOrEmpty(vo.getRkey())) {
//            return R.error(ResponseCode.DATA_ERROR.getMessage());
//        }
//
//        // 1.1 获取校验验证码
//        String redisCode = (String) redisTemplate.opsForValue().get(vo.getRkey());
//        // 1.2 校验
//        if (redisCode == null || !redisCode.equals(vo.getCode())) {
//            return R.error(ResponseCode.DATA_ERROR.getMessage());
//        }
//        // 1.3 redis删除key
//        redisTemplate.delete(vo.getRkey());
//
//        SysUser userInfo= sysUserMapper.findUserInfoByUserName(vo.getUsername());
//        if (userInfo==null) {
//            return R.error(ResponseCode.DATA_ERROR.getMessage());
//        }
//
//        // 3.判断密码,不匹配
//        if (!passwordEncoder.matches(vo.getPassword(),userInfo.getPassword())) {
//            return R.error(ResponseCode.SYSTEM_PASSWORD_ERROR.getMessage());
//        }
//
//        // 2、根据用户名查询用户权限相关信息
//        SysUser user = sysUserMapper.getUserPermissionInfo(vo.getUsername());
//        if (user == null) {
//            return R.error("未设置用户权限，禁止登入！");
//        }
//
//        // 将 user 中查询到的数据封装到 newLoginResVo 中
//        NewLoginReqVo result = new NewLoginReqVo();
//        BeanUtils.copyProperties(user, result);
//
//        // 将数据中的按钮权限封装到result中
//        List<String> permissions = new ArrayList<>();
//        List<SysPermission> sysPermissions = user.getPermissions();
//        for (SysPermission p : sysPermissions) {
//            if (!Strings.isNullOrEmpty(p.getPerms())) {
//                permissions.add(p.getPerms());
//            }
//        }
//        result.setPermissions(permissions);
//
//        // 处理权限树
//        // 获取跟节点，即父节点
//        List<PermissionDomain> menus = user.getPermissions().stream().filter(s -> "0".equals(s.getPid())).map(item -> {
//            PermissionDomain permissionDomain = new PermissionDomain();
//            permissionDomain.setId(item.getId());
//            permissionDomain.setName(item.getName());
//            permissionDomain.setIcon(item.getIcon());
//            permissionDomain.setTitle(item.getTitle());
//            permissionDomain.setPath(item.getUrl());
//            // 获取 SysPermission 类型的子权限树 传入的参数是 根节点的 id 和 采集到的 syspermission 类型数据
//            List<SysPermission> childMenus = getChildMenus(item.getId(), user.getPermissions());
//            item.setChildren(childMenus);
//            // 拷贝子权限树 类型转换 将 syspermission 类型的权限树 转换为 前端需要的 permissionDomain
//            permissionDomain.setChildren(new ArrayList<>());
//            permissionDomain.setChildren(copyChildren(item.getChildren(), permissionDomain.getChildren()));
//            return permissionDomain;
//        }).collect(Collectors.toList());
//        result.setMenus(menus);
//        return R.ok(result);
/*
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
*/
    }

    /**
     * 拷贝子权限树进行类型转换
     * @param source SysPermission
     * @param object PermissionDomain
     * @return
     */
    private List<PermissionDomain> copyChildren(List<SysPermission> source, List<PermissionDomain> object) {
        if (source != null && source.size() != 0) {
            for (SysPermission s : source) {
                PermissionDomain p = new PermissionDomain();
                p.setChildren(new ArrayList<PermissionDomain>());
                // 类型转换
                p.setId(s.getId());
                p.setTitle(s.getTitle());
                p.setIcon(s.getIcon());
                p.setPath(s.getUrl());
                p.setName(s.getName());
                object.add(p);
                // 判断子权限是否还有子权限，有的话，需要递归拷贝
                if (s.getChildren().size() != 0) {
                    copyChildren(s.getChildren(), p.getChildren());
                }
            }
        }
        return object;
    }

    /**
     * 递归获取子权限
     * @param id 父权限id
     * @param permissions 权限集合
     * @return
     */
    private List<SysPermission> getChildMenus(String id, List<SysPermission> permissions) {
        // 创建容器，存放权限
        List<SysPermission> children = new ArrayList<>();
        // 根据传过来的父权限id查询所有子权限
        permissions.forEach(s -> {
            if (id.equals(s.getPid())) {
                children.add(s);
            }
        });
        // 对每个子孩子进行递归
        children.forEach(s -> {
            s.setChildren(getChildMenus(s.getId(), permissions));
        });
        return children;
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

    /**
     * 多条件查询用户信息
     * @param req
     * @return
     */
    @Override
    public R<ConditionQueryUserResp> conditionQueryUser(ConditionalQueryUserReq req) {

        // 调用mapper接口
        int page = (Integer.parseInt(req.getPageNum()));
        int pageSize = Integer.parseInt(req.getPageSize());
        PageHelper.startPage(page, pageSize);

        List<SysUser> users = sysUserMapper.conditionQueryUser(req);
        ConditionQueryUserResp resp = new ConditionQueryUserResp();

        // 分页信息处理
        PageInfo<SysUser> pageInfo = new PageInfo<>(users);
        // 总条数
        resp.setTotalRows(pageInfo.getTotal());
        // 总页数
        resp.setTotalPages(pageInfo.getPages());
        // 当前页号
        resp.setPageNum(page);
        // 当前页大小
        resp.setPageSize(pageSize);
        // 当前页条数
        resp.setSize(users.size());
        // 信息
        resp.setRows(users);
        return R.ok(resp);
    }

    /**
     * 添加用户信息
     * @param adduser
     * @return
     */
    @Override
    public R addUsers(SysUser adduser) {
        // 调用mapper层方法
        // 全局主键设置
        adduser.setId(new IdWorker().nextId() + "");
        // 密码加密
        adduser.setPassword(passwordEncoder.encode(adduser.getPassword()));
        // 设置创建时间
        adduser.setCreateTime(DateTime.now().toDate());
        int insert = sysUserMapper.insertUser(adduser);
        if (insert == 0) {
            return R.error(ResponseCode.ERROR.getMessage());
        }
        return R.ok(ResponseCode.SUCCESS.getMessage());
    }

    /**
     * 批量删除用户信息，delete请求可通过请求体携带数据
     * @param userIds
     * @return
     */
    @Override
    public R<String> deleteByUserId(List<Long> userIds) {
        for (Long id : userIds) {
            int res = sysUserMapper.deleteByUserId(id);
            if (res == 0) {
                return R.error(ResponseCode.ERROR.getMessage());
            }
        }
        return R.ok(ResponseCode.SUCCESS.getMessage());
    }

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    @Override
    public R<GetUserInfoVo> getUserInfoById(Long userId) {
        GetUserInfoVo info = sysUserMapper.getUserInfoById(userId);
        System.out.println(info.toString());
        return R.ok(info);
    }

    /**
     * 更新用户信息
     * @param setUserInfoVo
     * @return
     */
    @Override
    public R<String> updateUserInfo(SetUserInfoVo setUserInfoVo) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(setUserInfoVo, user);

        Date updateTime = DateTime.now().toDate();
        user.setUpdateTime(updateTime);
        int col = sysUserMapper.updateUserInfo(user);

        if (col <= 0) {
            return R.error(ResponseCode.ERROR.getMessage());
        }
        return R.ok(ResponseCode.SUCCESS.getMessage());
    }
}
