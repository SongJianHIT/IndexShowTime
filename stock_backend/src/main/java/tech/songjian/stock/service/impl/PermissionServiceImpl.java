/**
 * @projectName stock_parent
 * @package tech.songjian.stock.service.impl
 * @className tech.songjian.stock.service.impl.PermissionSsrviceImpl
 */
package tech.songjian.stock.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.songjian.stock.common.domain.PermissionDomain;
import tech.songjian.stock.mapper.SysPermissionMapper;
import tech.songjian.stock.mapper.SysRolePermissionMapper;
import tech.songjian.stock.mapper.SysUserRoleMapper;
import tech.songjian.stock.pojo.SysPermission;
import tech.songjian.stock.pojo.SysRolePermission;
import tech.songjian.stock.service.PermissionService;
import tech.songjian.stock.vo.resp.PermissionTreeVo;
import tech.songjian.stock.vo.resp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PermissionSsrviceImpl
 * @description
 * @author SongJian
 * @date 2023/2/14 00:19
 * @version
 */
@Service("permissionService")
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    /**
     * 树状结构回显权限集合,底层通过递归获取权限数据集合
     * @return
     */
    @Override
    public R<List<PermissionDomain>> getPermissionTree() {
        List<SysPermission> lists = sysPermissionMapper.getAllPermission();
        List<PermissionDomain> permissionTreeVos = lists.stream().filter(s -> "0".equals(s.getPid())).map(item -> {
            PermissionDomain domain = new PermissionDomain();
            domain.setId(item.getId());
            domain.setTitle(item.getTitle());
            domain.setPath(item.getUrl());
            domain.setIcon(item.getIcon());
            domain.setName(item.getName());
            List<SysPermission> childrenSys = getChildrenPermission(item.getId(), lists);
            domain.setChildren(new ArrayList<>());
            domain.setChildren(copyChildrenPermission(childrenSys, domain.getChildren()));
            return domain;
        }).collect(Collectors.toList());
        return R.ok(permissionTreeVos);
    }

    /**
     * 根据用户id查询用户关联权限
     * @param roleId
     * @return
     */
    @Override
    public R<List> getPermissionByUserId(String roleId) {
        List<String> roles = sysRolePermissionMapper.getPermissionByUserId(roleId);
        return R.ok(roles);
    }

    /**
     * 递归拷贝
     * @param source
     * @param object
     * @return
     */
    private List<PermissionDomain> copyChildrenPermission(List<SysPermission> source, List<PermissionDomain> object) {
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
                    copyChildrenPermission(s.getChildren(), p.getChildren());
                }
            }
        }
        return object;
    }

    /**
     * 根据父权限id，递归封装children
     * @param Pid
     * @param permissions
     * @return
     */
    private List<SysPermission> getChildrenPermission(String Pid, List<SysPermission> permissions) {
        // 创建容器，存放权限
        List<SysPermission> children = new ArrayList<>();
        // 根据传过来的父权限id查询所有子权限
        permissions.forEach(s -> {
            if (Pid.equals(s.getPid())) {
                children.add(s);
            }
        });
        // 对每个子孩子进行递归
        children.forEach(s -> {
            s.setChildren(getChildrenPermission(s.getId(), permissions));
        });
        return children;
    }
}

