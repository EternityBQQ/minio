package com.itcast.minio.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @author zheng.zhang
 * Description 使用AOP记录Web日志
 * Date 2020/7/29 9:28
 * Version 1.0
 */
@Component
@Aspect
@Slf4j
public class WebLogAspect {

    /**
     * 切入点描述，这个是Controller层的切入点
     */
    @Pointcut("execution(public * com.itcast.minio.controller..*.*(..))")
    public void controllerLog() {} // 签名-->切入点名称

    /**
     * 在切入点的方法之前干的事
     * @param joinPoint 连接点
     */
    @Before("controllerLog()")
    public void logBeforeController(JoinPoint joinPoint) {
        // springMVC提供获得请求的方法
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            // 记录下请求内容
            log.info("URL：" + request.getRequestURL().toString());
            log.info("HTTP_METHOD：" + request.getMethod());
            log.info("IP：" + request.getRemoteAddr());
            log.info("THE ARGS OF CONTROLLER：" + Arrays.toString(joinPoint.getArgs()));

            // 下面这个getSignature().getDeclaringTypeName()是获取包+类名的   然后后面的joinPoint.getSignature.getName()获取了方法名
            log.info("CLASS_METHOD：" + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        }
    }
}
