package com.shopee.banking.bams.adapter.rest;

import com.shopee.banking.bams.api.api.request.SendAdminMessageRequest;
import com.shopee.banking.bams.common.exception.DependencyException;
import com.shopee.banking.bams.common.exception.ParamException;
import com.shopee.banking.bams.common.exception.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.result.Result;
import com.shopee.banking.bams.infra.mq.message.AdminMessage;
import com.shopee.banking.bams.infra.mq.producer.AdminMessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AdminMessageControllerTest {

    private static final Long ADMIN_ID = 1L;

    private AdminController adminController;

    @Mock
    private AdminMessageProducer adminMessageProducer;

    @BeforeEach
    void setUp() {
        adminController = new AdminController();
        ReflectionTestUtils.setField(adminController, "adminMessageProducer", adminMessageProducer);
    }

    @Test
    @DisplayName("sendAdminMessage succeeds with valid admin id")
    void sendAdminMessage_validAdminId_succeeds() {
        Result<Void> result = adminController.sendAdminMessage(buildSendAdminMessageRequest(ADMIN_ID));

        assertEquals(Result.SUCCESS_CODE, result.getCode());
        assertEquals(Result.SUCCESS_MSG, result.getMsg());
        assertNull(result.getData());

        ArgumentCaptor<AdminMessage> messageCaptor = ArgumentCaptor.forClass(AdminMessage.class);
        verify(adminMessageProducer).send(messageCaptor.capture());
        assertEquals(ADMIN_ID, messageCaptor.getValue().getAdminId());
        assertNotNull(messageCaptor.getValue().getOccurredAt());
        assertTrue(messageCaptor.getValue().getEventId() != null && !messageCaptor.getValue().getEventId().isBlank());
    }

    @Test
    @DisplayName("sendAdminMessage fails when admin id is null")
    void sendAdminMessage_nullAdminId_fails() {
        SendAdminMessageRequest request = buildSendAdminMessageRequest(null);

        assertThrows(ParamException.class, () -> adminController.sendAdminMessage(request));
        verifyNoInteractions(adminMessageProducer);
    }

    @Test
    @DisplayName("sendAdminMessage fails when admin id is not positive")
    void sendAdminMessage_nonPositiveAdminId_fails() {
        SendAdminMessageRequest request = buildSendAdminMessageRequest(0L);

        assertThrows(ParamException.class, () -> adminController.sendAdminMessage(request));
        verifyNoInteractions(adminMessageProducer);
    }

    @Test
    @DisplayName("sendAdminMessage surfaces producer failures")
    void sendAdminMessage_producerFailure_throwsDependencyException() {
        doThrow(new DependencyException(DependencyErrorCode.MQ_SEND_FAILED, "admin-message"))
                .when(adminMessageProducer).send(any(AdminMessage.class));

        DependencyException exception = assertThrows(
                DependencyException.class,
                () -> adminController.sendAdminMessage(buildSendAdminMessageRequest(ADMIN_ID))
        );

        assertEquals(DependencyErrorCode.MQ_SEND_FAILED, exception.getErrorType());
        verify(adminMessageProducer).send(any(AdminMessage.class));
    }

    private SendAdminMessageRequest buildSendAdminMessageRequest(Long adminId) {
        SendAdminMessageRequest request = new SendAdminMessageRequest();
        ReflectionTestUtils.setField(request, "adminId", adminId);
        return request;
    }
}
