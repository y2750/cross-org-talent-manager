package com.crossorgtalentmanager.aop;

import com.crossorgtalentmanager.annotation.AuthCheck;
import com.crossorgtalentmanager.exception.BusinessException;
import com.crossorgtalentmanager.exception.ErrorCode;
import com.crossorgtalentmanager.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import com.crossorgtalentmanager.model.entity.User;
import com.crossorgtalentmanager.model.enums.UserRoleEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // 不需要权限，放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 以下为：必须有该权限才通过
        // 获取当前用户具有的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有权限，拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 角色等级比较：admin > company_admin > hr > employee
        if (roleRank(userRoleEnum) < roleRank(mustRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }

    /**
     * 简单的角色等级映射，数值越大权限越高
     */
    private int roleRank(UserRoleEnum role) {
        if (role == null) {
            return 0;
        }
        switch (role) {
            case ADMIN:
                return 4;
            case COMPANY_ADMIN:
                return 3;
            case HR:
                return 2;
            case EMPLOYEE:
                return 1;
            default:
                return 0;
        }
    }
}
