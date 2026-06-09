package com.shopee.banking.bams.infra.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.core.RocketMQListener;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class BaseMessageConsumer<T> implements RocketMQListener<MessageExt> {

    private final ObjectMapper objectMapper;

    protected BaseMessageConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(MessageExt messageExt) {
        String payload = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        try {
            T message = objectMapper.readValue(payload, getMessageType());
            handleMessage(message, messageExt);
        } catch (Exception exception) {
            log.error("Failed to consume RocketMQ message. topic={}, msgId={}, payload={}",
                    messageExt.getTopic(), messageExt.getMsgId(), payload, exception);
        }
    }

    protected abstract Class<T> getMessageType();

    protected abstract void handleMessage(T message, MessageExt rawMessage);
}
