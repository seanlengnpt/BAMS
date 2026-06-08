package com.shopee.banking.bams.adapter.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopee.banking.bams.infra.mq.consumer.BaseMessageConsumer;
import com.shopee.banking.bams.infra.mq.message.AdminMessage;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "bams.mq.admin-message", name = "consumer-enabled", havingValue = "true", matchIfMissing = true)
@RocketMQMessageListener(
        topic = "${bams.mq.admin-message.topic}",
        consumerGroup = "${bams.mq.admin-message.consumer-group}"
)
public class AdminMessageConsumer extends BaseMessageConsumer<AdminMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminMessageConsumer.class);

    public AdminMessageConsumer(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected Class<AdminMessage> getMessageType() {
        return AdminMessage.class;
    }

    @Override
    protected void handleMessage(AdminMessage message, MessageExt rawMessage) {
        LOGGER.info("Consumed admin message. topic={}, msgId={}, adminId={}, eventId={}",
                rawMessage.getTopic(),
                rawMessage.getMsgId(),
                message.getAdminId(),
                message.getEventId());
    }
}
