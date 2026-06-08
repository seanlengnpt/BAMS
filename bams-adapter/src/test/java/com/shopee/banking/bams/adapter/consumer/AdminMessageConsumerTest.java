package com.shopee.banking.bams.adapter.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AdminMessageConsumerTest {

    private AdminMessageConsumer adminMessageConsumer;

    @BeforeEach
    void setUp() {
        adminMessageConsumer = new AdminMessageConsumer(new ObjectMapper());
    }

    @Test
    @DisplayName("onMessage handles valid payload without throwing")
    void onMessage_validPayload_doesNotThrow() {
        MessageExt messageExt = buildMessage("{\"adminId\":1,\"eventId\":\"event-1\"}");

        assertDoesNotThrow(() -> adminMessageConsumer.onMessage(messageExt));
    }

    @Test
    @DisplayName("onMessage swallows invalid payload errors")
    void onMessage_invalidPayload_doesNotThrow() {
        MessageExt messageExt = buildMessage("not-json");

        assertDoesNotThrow(() -> adminMessageConsumer.onMessage(messageExt));
    }

    private MessageExt buildMessage(String payload) {
        MessageExt messageExt = new MessageExt();
        messageExt.setTopic("bams-admin-message-topic");
        messageExt.setMsgId("msg-1");
        messageExt.setBody(payload.getBytes(StandardCharsets.UTF_8));
        return messageExt;
    }
}
