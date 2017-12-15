package com.pdwu.easycloud.user.interceptor;

import com.pdwu.easycloud.common.bean.ResultBean;
import com.pdwu.easycloud.common.bean.ResultCode;
import com.pdwu.easycloud.common.util.JsonUtils;
import com.pdwu.easycloud.common.util.WebUtils;
import com.pdwu.easycloud.user.bean.TokenBean;
import com.pdwu.easycloud.user.service.ITokenService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private ITokenService tokenService;

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        boolean isPublic = WebUtils.checkUriPublic(httpServletRequest.getRequestURI());

        String token = httpServletRequest.getParameter("token");

        //1. 不带token
        if (StringUtils.isBlank(token)) {
            //公共接口，可通过; 否则拦截
            return isPublic || returnTokenInvalid(httpServletRequest, httpServletResponse, o);
        }

        //2. 带了token，则认证
        ResultBean resultBean = tokenService.checkTokenValid(token);

        //2.1 token无效
        if (resultBean.getCode() != ResultCode.ok) {
            //公共接口，可通过; 否则拦截
            return isPublic || returnTokenInvalid(httpServletRequest, httpServletResponse, o);
        }

        //2.2 有效
        TokenBean tokenBean = (TokenBean) resultBean.getData();
        httpServletRequest.getSession().setAttribute("userId", tokenBean.getUserId());


        return true;
    }

    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    private boolean returnTokenInvalid(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws IOException {

        PrintWriter writer = httpServletResponse.getWriter();

        writer.write(JsonUtils.objectToJson(ResultBean.fail(403, "please login")));

        return false;

    }

}
