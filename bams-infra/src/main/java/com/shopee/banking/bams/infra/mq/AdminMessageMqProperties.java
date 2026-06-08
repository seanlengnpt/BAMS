package com.shopee.banking.bams.infra.mq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bams.mq.admin-message")
public class AdminMessageMqProperties {
    private String topic;
}
