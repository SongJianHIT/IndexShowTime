/**
 * @projectName stock_parent
 * @package tech.songjian.stock.service.impl
 * @className tech.songjian.stock.service.impl.RoleServiceImpl
 */
package tech.songjian.stock.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.songjian.stock.common.domain.OwnRoleAndAllRoleIdsDomain;
import tech.songjian.stock.common.domain.StockUpdownDomain;
import tech.songjian.stock.mapper.SysRoleMapper;
import tech.songjian.stock.mapper.SysUserRoleMapper;
import tech.songjian.stock.pojo.SysRole;
import tech.songjian.stock.pojo.SysUserRole;
import tech.songjian.stock.service.RoleService;
import tech.songjian.stock.utils.IdWorker;
import tech.songjian.stock.vo.req.UpdateRoleInfoReq;
import tech.songjian.stock.vo.resp.PageResult;
import tech.songjian.stock.vo.resp.R;
import tech.songjian.stock.vo.resp.ResponseCode;

import java.util.Date;
import java.util.List;

/**
 * RoleServiceImpl
 * @description
 * @author SongJian
 * @date 2023/2/13 20:01
 * @version
 */
@Service("roleService")
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 获取用户具有的角色信息，以及所有角色信息
     * @param userId
     * @return
     */
    @Override
    public R<OwnRoleAndAllRoleIdsDomain> getUsersRoles(String userId) {
        OwnRoleAndAllRoleIdsDomain domain = new OwnRoleAndAllRoleIdsDomain();
        List<SysRole> allRoles = sysRoleMapper.getAllRoles();
        List<String> ids = sysUserRoleMapper.queryRolesById(userId);
        domain.setAllRole(allRoles);
        domain.setOwnRoleIds(ids);
        return R.ok(domain);
    }

    /**
     * 更新用户角色信息
     * @param req
     * @return
     */
    @Override
    public R<String> updateRoleInfo(UpdateRoleInfoReq req) {
        // 调用mapper层方法
        sysUserRoleMapper.deleteByUserId(req.getUserId());
        List<String> roleIds = req.getRoleIds();
        for (String roleId : roleIds) {
            long primaryKey = new IdWorker().nextId();
            Date updateTime = DateTime.now().toDate();
            sysUserRoleMapper.inserByUserRoleIds(primaryKey, req.getUserId(), roleId, updateTime);
        }
        return R.ok(ResponseCode.SUCCESS.getMessage());
    }

    /**
     * 分页查询角色信息
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public R<PageResult<SysRole>> getRolesInfoByPage(Integer pageNum, Integer pageSize) {
        // 开启 PageHelper
        PageHelper.startPage(pageNum, pageSize);
        List<SysRole> pages = sysRoleMapper.getRolesInfo();
        if (CollectionUtils.isEmpty(pages)) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        // 3、组装 pageInfo 对象，他封装了一切的分页信息
        PageInfo<SysRole> pageInfo = new PageInfo<>(pages);
        PageResult<SysRole> pageResult = new PageResult<>(pageInfo);
        return R.ok(pageResult);
    }
}

