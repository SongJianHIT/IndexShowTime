package tech.songjian.stock.security.service.impl;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tech.songjian.stock.mapper.SysPermissionMapper;
import tech.songjian.stock.mapper.SysRoleMapper;
import tech.songjian.stock.mapper.SysUserMapper;
import tech.songjian.stock.pojo.SysPermission;
import tech.songjian.stock.pojo.SysRole;
import tech.songjian.stock.pojo.SysUser;
import tech.songjian.stock.service.PermissionService;
import tech.songjian.stock.service.UserService;
import tech.songjian.stock.vo.resp.PermissionTreeVo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by itheima
 * @Date 2021/12/24
 * @Description
 */
@Component
public class LoginUserDetailService implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserService userService;

    /**
     * 根据用户名获取用户详情
     * @param userName 用户名
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        SysUser user = this.sysUserMapper.findUserByUserName(userName);
        // ThreadLocalUtils.setVal(user);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在！");
        }
        //获取权限集合
        List<SysPermission> permissionList = this.sysPermissionMapper.getPermissionByUserId(user.getId());
        List<String> permsNameList = permissionList.stream().filter(item -> !Strings.isNullOrEmpty(item.getPerms())).map(item -> item.getPerms())
                .collect(Collectors.toList());

        //获取角色集合 基于角色鉴权注解需要将角色前追加ROLE_
        List<SysRole> roleList= sysRoleMapper.getRoleByUserId(user.getId());
        //角色表示需要追加前缀ROLE_
        List<String> roleNameList = roleList.stream().filter(item -> !Strings.isNullOrEmpty(item.getName()))
                .map(item ->  "ROLE_" + item.getName()).collect(Collectors.toList());
        List<String> auths= new ArrayList<String>();
        auths.addAll(permsNameList);
        auths.addAll(roleNameList);

        //转化为数组
        String[] perms=auths.toArray(new String[auths.size()]);

        //转化为数组，给springSecurity的
        List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(perms);
        user.setAuthorities(authorityList);

        //权限树结构，给前端响应
        List<PermissionTreeVo> treeNodeVo = permissionService.getTree(permissionList, "0", true);
        user.setMenus(treeNodeVo);

        //按钮权限集合，给前端响应
        List<String> authBtnPerms=null;
        // 用户拥有的按钮权限
        if (!CollectionUtils.isEmpty(permissionList)) {
            authBtnPerms = permissionList.stream().filter(per -> !Strings.isNullOrEmpty(per.getCode()) && per.getType()==3)
                    .map(per -> per.getCode()).collect(Collectors.toList());
        }
        user.setPermissions(authBtnPerms);
        return user;
    }
}
