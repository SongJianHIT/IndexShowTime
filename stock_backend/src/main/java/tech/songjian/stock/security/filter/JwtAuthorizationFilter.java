package tech.songjian.stock.security.filter;

import com.google.gson.Gson;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.songjian.stock.security.utils.JwtTokenUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

public class JwtAuthorizationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //1.从http请求头中获取token
        String token = request.getHeader(JwtTokenUtil.TOKEN_HEADER);
        if (token==null) {
            //用户未登录,则放行，去登录拦截
            filterChain.doFilter(request,response);
            return;
        }
        //2.token存在则，安全校验
        try {
            String username = JwtTokenUtil.getUsername(token);
            //获取以逗号间隔的权限拼接字符串
            String userRole = JwtTokenUtil.getUserRole(token);
            //组装token
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.commaSeparatedStringToAuthorityList(userRole));
            //将生成的token存入上下文
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            //放行资源
            filterChain.doFilter(request,response);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException("token无效");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            HashMap<String, String> info = new HashMap<>();
            info.put("status","0");
            info.put("ex","无效的token凭证");
            response.getWriter().write(new Gson().toJson(info));
        }
    }
}
