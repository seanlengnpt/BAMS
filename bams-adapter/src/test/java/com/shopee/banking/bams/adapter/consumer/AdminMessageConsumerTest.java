package com.shopee.banking.bams.adapter.consumer;

import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class AdminMessageConsumerTest {

    private AdminMessageConsumer adminMessageConsumer;

    @BeforeEach
    void setUp() {
        adminMessageConsumer = new AdminMessageConsumer(new ObjectMapper());
    }

    @Test
    @DisplayName("onMessage handles valid payload without throwing")
    void onMessage_validPayload_logsConsumedMessage(CapturedOutput output) {
        MessageExt messageExt = buildMessage("{\"adminId\":1,\"eventId\":\"event-1\"}");

        assertDoesNotThrow(() -> adminMessageConsumer.onMessage(messageExt));
        assertTrue(output.getAll().contains("Consumed admin message. topic=bams-admin-message-topic, msgId=msg-1, adminId=1, eventId=event-1"));
    }

    @Test
    @DisplayName("onMessage swallows invalid payload errors")
    void onMessage_invalidPayload_logsErrorWithoutThrowing(CapturedOutput output) {
        MessageExt messageExt = buildMessage("not-json");

        assertDoesNotThrow(() -> adminMessageConsumer.onMessage(messageExt));
        assertTrue(output.getAll().contains("Failed to consume RocketMQ message. topic=bams-admin-message-topic, msgId=msg-1, payload=not-json"));
    }

    private MessageExt buildMessage(String payload) {
        MessageExt messageExt = new MessageExt();
        messageExt.setTopic("bams-admin-message-topic");
        messageExt.setMsgId("msg-1");
        messageExt.setBody(payload.getBytes(StandardCharsets.UTF_8));
        return messageExt;
    }
}
