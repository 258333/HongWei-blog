package com.hongwei.handler;


import com.hongwei.constants.Const;
import com.hongwei.constants.RespConst;
import com.hongwei.domain.entity.LoginUser;
import com.hongwei.domain.response.ResponseResult;
import com.hongwei.domain.vo.AuthorizeVO;
import com.hongwei.enums.RespEnum;
import com.hongwei.service.LoginLogService;
import com.hongwei.service.UserService;
import com.hongwei.utils.JwtUtils;
import com.hongwei.utils.RedisCache;
import com.hongwei.utils.StringUtils;
import com.hongwei.utils.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author: HongWei
 * @date: 2024/11/14 14:58
 *
 * @description:
 **/
@Component
@RequiredArgsConstructor
public class SecurityHandler {


    private final JwtUtils jwtUtils;

    private final RedisCache redisCache;

    private final LoginLogService loginLogService;

    @Lazy
    private final UserService userService;

    public static final String USER_NAME = "username";


    /**
     * 登录成功处理
     *
     * @param request        请求
     * @param response       响应
     * @param authentication 认证信息
     */
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            // Authentication 是 Spring Security 中用于封装认证信息的接口，
            // 表示用户的身份验证信息。它包含了用户的相关信息，如用户名、密码、权限、凭证等
            Authentication authentication
    ) {
        handlerOnAuthenticationSuccess(request,response,(LoginUser)authentication.getPrincipal());
    }

    public void handlerOnAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            LoginUser user
    ) {
        String typeHeader = request.getHeader(Const.TYPE_HEADER);
        if ((!StringUtils.matches(typeHeader, List.of(Const.BACKEND_REQUEST, Const.FRONTEND_REQUEST)) && user.getUser().getRegisterType() == 1)) {
            throw new BadCredentialsException("非法请求");
        }
        Long id = user.getUser().getId();
        String name = user.getUser().getUsername();
        // UUID做jwt的id
        String uuid = UUID.randomUUID().toString();
        // 生成jwt
        String token = jwtUtils.createJwt(uuid, user, id, name);

        // 转换VO
        AuthorizeVO authorizeVO = user.getUser().asViewObject(AuthorizeVO.class, v -> {
            v.setToken(token);
            v.setExpire(jwtUtils.expireTime());
        });
        // TODO 造成依赖循环
        userService.userLoginStatus(user.getUser().getId(), user.getUser().getRegisterType());
        loginLogService.loginLog(request, request.getParameter(USER_NAME), 0, RespConst.SUCCESS_LOGIN_MSG);
        WebUtil.renderString(response, ResponseResult.success(authorizeVO, RespConst.SUCCESS_LOGIN_MSG).asJsonString());
    }


    /**
     * 登录失败处理
     */
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        loginLogService.loginLog(request, request.getParameter(USER_NAME), 1, exception.getMessage());
        WebUtil.renderString(response, ResponseResult.failure(RespEnum.USERNAME_OR_PASSWORD_ERROR.getCode(), exception.getMessage()).asJsonString());
    }

    /**
     * 退出登录处理
     */
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        boolean invalidateJwt = jwtUtils.invalidateJwt(request.getHeader("Authorization"));
        if (invalidateJwt) {
            WebUtil.renderString(response, ResponseResult.success().asJsonString());
            return;
        }
        WebUtil.renderString(response, ResponseResult.failure(RespEnum.NOT_LOGIN.getCode(), RespEnum.NOT_LOGIN.getMsg()).asJsonString());
    }

    /**
     * 没有登录处理
     */
    public void onUnAuthenticated(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        WebUtil.renderString(response, ResponseResult.failure(RespEnum.NOT_LOGIN.getCode(), RespEnum.NOT_LOGIN.getMsg()).asJsonString());
    }

    /**
     * 没有权限处理
     */
    public void onAccessDeny(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) {
        WebUtil.renderString(response, ResponseResult.failure(RespEnum.NO_PERMISSION.getCode(), RespEnum.NO_PERMISSION.getMsg()).asJsonString());
    }
}
