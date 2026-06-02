package com.shopee.banking.bams.adapter.aspect;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.common.util.Asserter;
import com.shopee.banking.bams.common.exception.BaseException;
import com.shopee.banking.bams.common.exception.enums.SystemErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
@Order(Integer.MIN_VALUE + 2)
public class ControllerAspect {
    @Value("${bams.auth.jwt.secret}")
    private String jwtSecret;

    @Pointcut("within(com.shopee.banking.bams.adapter..*)")
    public void bankingAdapter() {
    }
    /**
     * annotated with @RestController
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restController() {
    }

    /**
     * annotated with @RequestMapping
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void requestMapping() {
    }

    /**
     * annotated with @GetMapping
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void getMapping() {
    }

    /**
     * annotated with @PostMapping
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {
    }

    @Around("bankingAdapter() && restController() && (requestMapping() || getMapping() || postMapping())")
    public Object aroundControllers(ProceedingJoinPoint joinPoint) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        Object requestBody = joinPoint.getArgs().length > 0
                ? joinPoint.getArgs()[0]
                : null;

        Object response = null;
        if (HttpMethod.POST.name().equals(servletRequest.getMethod())) {
            Asserter.assertNotNull(requestBody, ParamErrorCode.INVALID_PARAM);
        }
        try{
            response = joinPoint.proceed();
        }catch (BaseException e){
            return Result.fail(e);
        }catch (Throwable T){
            System.out.println(T.getMessage());
            return Result.fail(SystemErrorCode.UNKNOWN_EXCEPTION);
        }
        return response;
    }
}
