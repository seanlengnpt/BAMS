package com.shopee.banking.bams.infra.mq.producer;

import com.shopee.banking.bams.common.exception.DependencyException;
import com.shopee.banking.bams.infra.mq.AdminMessageMqProperties;
import com.shopee.banking.bams.infra.mq.message.AdminMessage;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMessageProducerTest {

    private AdminMessageProducer adminMessageProducer;

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    private AdminMessageMqProperties adminMessageMqProperties;

    @BeforeEach
    void setUp() {
        adminMessageMqProperties = new AdminMessageMqProperties();
        adminMessageMqProperties.setTopic("bams-admin-message-topic");
        adminMessageProducer = new AdminMessageProducer(rocketMQTemplate, new ObjectMapper(), adminMessageMqProperties);
    }

    @Test
    @DisplayName("send serializes payload and uses configured topic")
    void send_validMessage_sendsToConfiguredTopic() {
        SendResult sendResult = new SendResult();
        sendResult.setSendStatus(SendStatus.SEND_OK);
        sendResult.setMsgId("msg-1");
        when(rocketMQTemplate.syncSend(eq("bams-admin-message-topic"), any(Message.class))).thenReturn(sendResult);

        adminMessageProducer.send(AdminMessage.builder()
                .adminId(1L)
                .eventId("event-1")
                .occurredAt(LocalDateTime.of(2026, 6, 8, 12, 0))
                .build());

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(rocketMQTemplate).syncSend(eq("bams-admin-message-topic"), messageCaptor.capture());
        assertEquals("1", messageCaptor.getValue().getHeaders().get("KEYS"));
        String payload = (String) messageCaptor.getValue().getPayload();
        assertTrue(payload.contains("\"adminId\":1"));
        assertTrue(payload.contains("\"eventId\":\"event-1\""));
    }

    @Test
    @DisplayName("send converts RocketMQ failures to dependency exception")
    void send_rocketMqFailure_throwsDependencyException() {
        when(rocketMQTemplate.syncSend(eq("bams-admin-message-topic"), any(Message.class)))
                .thenThrow(new IllegalStateException("boom"));

        DependencyException exception = assertThrows(
                DependencyException.class,
                () -> adminMessageProducer.send(AdminMessage.builder().adminId(1L).build())
        );

        assertEquals(20005, exception.getErrorType().getCode());
    }
}
