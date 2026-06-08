package com.shopee.banking.bams.infra.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopee.banking.bams.common.exception.enums.DependencyErrorCode;
import com.shopee.banking.bams.common.exception.factory.ExceptionFactory;
import com.shopee.banking.bams.infra.mq.AdminMessageMqProperties;
import com.shopee.banking.bams.infra.mq.message.AdminMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminMessageProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;
    private final AdminMessageMqProperties adminMessageMqProperties;

    public void send(AdminMessage message) {
        try {
            String payload = objectMapper.copy().findAndRegisterModules().writeValueAsString(message);
            Message<String> mqMessage = MessageBuilder.withPayload(payload)
                    .setHeader(RocketMQHeaders.KEYS, String.valueOf(message.getAdminId()))
                    .build();

            SendResult sendResult = rocketMQTemplate.syncSend(adminMessageMqProperties.getTopic(), mqMessage);
            log.info("Admin message sent. topic={}, adminId={}, sendStatus={}, msgId={}",
                    adminMessageMqProperties.getTopic(),
                    message.getAdminId(),
                    sendResult.getSendStatus(),
                    sendResult.getMsgId());
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize admin message. adminId={}", message.getAdminId(), exception);
            ExceptionFactory.throwException(DependencyErrorCode.MQ_SEND_FAILED, "admin-message");
        } catch (Exception exception) {
            log.error("Failed to send admin message. topic={}, adminId={}",
                    adminMessageMqProperties.getTopic(), message.getAdminId(), exception);
            ExceptionFactory.throwException(DependencyErrorCode.MQ_SEND_FAILED, "admin-message");
        }
    }
}
