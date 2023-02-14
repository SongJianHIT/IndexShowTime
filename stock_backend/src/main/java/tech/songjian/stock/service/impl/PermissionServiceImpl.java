/**
 * @projectName stock_parent
 * @package tech.songjian.stock.service.impl
 * @className tech.songjian.stock.service.impl.PermissionSsrviceImpl
 */
package tech.songjian.stock.service.impl;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.songjian.stock.common.domain.PermissionDomain;
import tech.songjian.stock.mapper.SysPermissionMapper;
import tech.songjian.stock.mapper.SysRolePermissionMapper;
import tech.songjian.stock.pojo.SysPermission;
import tech.songjian.stock.service.PermissionService;
import tech.songjian.stock.utils.IdWorker;
import tech.songjian.stock.vo.resp.R;
import tech.songjian.stock.vo.resp.ResponseCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private IdWorker idWorker;

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
     * 获取所有权限集合
     * @return
     */
    @Override
    public R<List<SysPermission>> getAllPermissions() {
        List<SysPermission> permissions =  sysPermissionMapper.getAllPermissions();
        return R.ok(permissions);
    }

    /**
     * 添加权限时回显权限树，仅仅显示目录和菜单
     * @return
     */
    List<Map> mapList = new ArrayList<>();

    @Override
    public R<List<Map>> getPermissionsTree4Add() {
        //按照用户id查找其权限信息
        List<Map> list = sysPermissionMapper.findAllPermissionlLevel1();

        Map<String,Object> mostLevel = new HashMap<>();
        mostLevel.put("id","0");
        mostLevel.put("title","顶级菜单");
        mostLevel.put("level",0);
        mapList.add(mostLevel);
        for (Map map : list) {
            map.put("level",1);
        }
        int i = 1;
        //List result = extracted(list,i);
        extracted(list);

        return R.ok(mapList);
    }

    @Override
    public R<String> addPermission(SysPermission sysPermission) {
        sysPermission.setId(String.valueOf(idWorker.nextId()));
        if (sysPermission.getType() == 1) {
            sysPermission.setUrl(null);
        }
        sysPermission.setCreateTime(DateTime.now().toDate());
        sysPermission.setDeleted(1);
        sysPermission.setStatus(1);
        int i = sysPermissionMapper.addPermission(sysPermission);
        if (i > 0) {
            return R.ok(ResponseCode.SUCCESS.getMessage());
        }
        return R.error(ResponseCode.ERROR.getMessage());
    }

    private void extracted(List<Map> list) {
        for (Map map : list) {
            mapList.add(map);
            String id = (String) map.get("id");
            //寻找权限下的子权限
            List<Map> sonPermissionSon = sysPermissionMapper.findAllPermissionSon(id);

            if (sonPermissionSon != null) {
                if (sonPermissionSon.size() != 0) {
                    for (Map map1 : sonPermissionSon) {
                        int level = (int) map.get("level");
                        map1.put("level", level + 1);
                        //mapList.add(map1);
                    }
                    //i++;
                    extracted(sonPermissionSon);
                }
            }

        }
    }
    /**
     * 递归拷贝
     *
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

