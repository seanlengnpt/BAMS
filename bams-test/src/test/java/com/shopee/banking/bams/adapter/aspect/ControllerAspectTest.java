package com.shopee.banking.bams.adapter.aspect;

import com.shopee.banking.bams.common.exception.DependencyException;
import com.shopee.banking.bams.common.exception.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.exception.enums.ParamErrorCode;
import com.shopee.banking.bams.common.exception.enums.SystemErrorCode;
import com.shopee.banking.bams.common.result.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControllerAspectTest {

    private ControllerAspect controllerAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        controllerAspect = new ControllerAspect();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("POST request with missing body returns NULL_PARAM failure result")
    void aroundControllers_postRequestMissingBody_returnsNullParamFailure() throws Throwable {
        setRequestMethod("POST");
        when(joinPoint.getArgs()).thenReturn(new Object[0]);

        Object response = controllerAspect.aroundControllers(joinPoint);

        Result<?> result = assertInstanceOf(Result.class, response);
        assertEquals(ParamErrorCode.NULL_PARAM.getCode(), result.getCode());
        assertEquals(ParamErrorCode.NULL_PARAM.getMsg(), result.getMsg());
        assertNull(result.getData());
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("Valid response returned by proceed is forwarded unchanged")
    void aroundControllers_validResponse_returnsProceedResult() throws Throwable {
        setRequestMethod("POST");
        Result<String> expected = Result.success("ok");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"request-body"});
        when(joinPoint.proceed()).thenReturn(expected);

        Object response = controllerAspect.aroundControllers(joinPoint);

        assertSame(expected, response);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("BaseException thrown by proceed is converted to failure result")
    void aroundControllers_baseException_returnsFailureResult() throws Throwable {
        setRequestMethod("GET");
        DependencyException exception = new DependencyException(
                DependencyErrorCode.DATABASE_QUERY_FAILED,
                "customer"
        );
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenThrow(exception);

        Object response = controllerAspect.aroundControllers(joinPoint);

        Result<?> result = assertInstanceOf(Result.class, response);
        assertEquals(DependencyErrorCode.DATABASE_QUERY_FAILED.getCode(), result.getCode());
        assertEquals("Database query failed for customer.", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Unknown throwable thrown by proceed is converted to UNKNOWN_EXCEPTION failure result")
    void aroundControllers_unknownThrowable_returnsUnknownFailureResult() throws Throwable {
        setRequestMethod("GET");
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        Object response = controllerAspect.aroundControllers(joinPoint);

        Result<?> result = assertInstanceOf(Result.class, response);
        assertEquals(SystemErrorCode.UNKNOWN_EXCEPTION.getCode(), result.getCode());
        assertEquals(SystemErrorCode.UNKNOWN_EXCEPTION.getMsg(), result.getMsg());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Non-POST request does not require a body")
    void aroundControllers_nonPostRequestWithoutBody_proceedsNormally() throws Throwable {
        setRequestMethod("GET");
        Result<Void> expected = Result.success();
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenReturn(expected);

        Object response = controllerAspect.aroundControllers(joinPoint);

        assertSame(expected, response);
        verify(joinPoint).proceed();
    }

    private void setRequestMethod(String method) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
