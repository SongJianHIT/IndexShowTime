package tech.songjian.stock.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import tech.songjian.stock.pojo.SysUser;
import tech.songjian.stock.security.utils.JwtTokenUtil;
import tech.songjian.stock.vo.resp.LoginRespVo;
import tech.songjian.stock.vo.resp.R;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author by itheima
 * @Date 2021/12/26
 * @Description 自定义登录过滤器
 */
public class JwtLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private RedisTemplate redisTemplate;

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 自定义的登录路径
     * @param loginUrl
     */
    public JwtLoginAuthenticationFilter(String loginUrl) {
        super(loginUrl);
    }

    /**
     * 认证过滤
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String username=null;
        String password=null;
        //验证码
        String checkCode=null;
        //redis中rkeyS
        String rkey=null;
        //判断当前是form登录还是ajax登录
        if (request.getContentType().equalsIgnoreCase(MediaType.APPLICATION_JSON_VALUE) ||
 request.getContentType().equalsIgnoreCase(MediaType.APPLICATION_JSON_UTF8_VALUE)) {
            try {
                // 从输入流中获取到登录的信息
                ServletInputStream in = request.getInputStream();
                HashMap<String,String> map = new ObjectMapper().readValue(in, HashMap.class);
                username=map.get("username");
                password=map.get("password");
                //获取校验码
                checkCode=map.get("code");
                rkey=map.get("rkey");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            //支持form表单提交方式
            username = request.getParameter("username");
            password=request.getParameter("password");
            checkCode=request.getParameter("code");
            rkey=request.getParameter("rkey");
        }
        String rkeyValue = (String) redisTemplate.opsForValue().get(rkey);
        if (rkeyValue==null || !rkeyValue.equals(checkCode)) {
            throw new RuntimeException("验证码错误");
        }
        //删除验证码
        redisTemplate.delete(rkey);
        //生成认证token
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username,password);
       //构建token后调用authenticationManager.authenticate()方法让spring-security去进行验证
        return this.getAuthenticationManager().authenticate(token);
    }

    /**
     * 认证成功后的处理方法
     * @param request
     * @param response
     * @param chain
     * @param authResult
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SysUser user= (SysUser) authResult.getPrincipal();
        // 从User中获取权限信息
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        // 创建Token
        String token = JwtTokenUtil.createToken(user.getUsername(), authorities.toString());
        //设置响应编码格式
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        //组装响应结果
        LoginRespVo loginResult = LoginRespVo.builder().id(user.getId())
                .nickName(user.getNickName())
                .username(user.getUsername())
                .phone(user.getPhone())
                .menus(user.getMenus())
                .permissions(user.getPermissions())
                .accessToken(token)
                .build();
        R<LoginRespVo> ok = R.ok(loginResult);
        //转化成json字符串响应前端
        String result = new Gson().toJson(ok);
        //响应数据
        response.getWriter().write(result);
    }

    /**
     * 认证失败后的处理方法
     * @param request
     * @param response
     * @param failed
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        String returnData="";
        // 账号过期
        if (failed instanceof AccountExpiredException) {
            returnData="账号过期";
        }
        // 密码错误
        else if (failed instanceof BadCredentialsException) {
            returnData="密码错误";
        }
        // 密码过期
        else if (failed instanceof CredentialsExpiredException) {
            returnData="密码过期";
        }
        // 账号不可用
        else if (failed instanceof DisabledException) {
            returnData="账号不可用";
        }
        //账号锁定
        else if (failed instanceof LockedException) {
            returnData="账号锁定";
        }
        // 用户不存在
        else if (failed instanceof InternalAuthenticationServiceException) {
            returnData="用户不存在";
        }
        // 其他错误
        else{
            returnData="未知异常";
        }
        // 处理编码方式 防止中文乱码
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        // 将反馈塞到HttpServletResponse中返回给前台
        R result = R.error(returnData);
        response.getWriter().write(new Gson().toJson(result));
    }
}
