package com.hongwei.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.hongwei.domain.entity.LoginUser;
import com.hongwei.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

/**
 * @author: HongWei
 * @date: 2024/11/14 16:32
 **/
@Component
@RequiredArgsConstructor
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 提取 Header
        String authorization = request.getHeader("Authorization");
        // 解析jwt
        DecodedJWT jwt = jwtUtils.resolveJwt(authorization);

        if (!ObjectUtils.isEmpty(jwt)) {
            // 获取UserDetails
            LoginUser user = (LoginUser) jwtUtils.toUser(jwt);
            // 创建认证对象
            //创建一个 UsernamePasswordAuthenticationToken 对象，代表用户认证信息。
            //user: LoginUser 对象，包含了当前登录用户的信息。
            //null: 这是密码部分，这里为 null 因为使用 JWT 时，密码不需要再验证。
            //user.getAuthorities(): 获取用户的权限信息（通常是角色权限列表），这个会用来进行权限判断。
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            // 保存认证详细信息
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 验证通过，设置上下文中
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
