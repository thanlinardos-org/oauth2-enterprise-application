package com.thanlinardos.resource_server.aspect;

import com.thanlinardos.spring_enterprise_library.error.errorcodes.ErrorCode;
import com.thanlinardos.spring_enterprise_library.error.exceptions.CoreException;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.api.service.PrivilegedResourceService;
import com.thanlinardos.spring_enterprise_library.spring_cloud_security.aspect.AuthorizationAspectHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
@Profile("controller_authorization")
public class ControllerAuthorizationAspect {

    private final PrivilegedResourceService privilegedResourceService;

    @Around("com.thanlinardos.resource_server.aspect.PointCutDefinitions.forControllerPackage()")
    private Object authorizeControllerOperation(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            return AuthorizationAspectHelper.authorizeControllerOperation(proceedingJoinPoint, privilegedResourceService);
        } catch (Exception e) {
            throw new CoreException(ErrorCode.UNEXPECTED_ERROR, "Error authorizing controller operation", e);
        }
    }
}
