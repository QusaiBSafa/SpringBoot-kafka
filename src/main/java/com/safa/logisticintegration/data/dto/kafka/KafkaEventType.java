package com.safa.logisticintegration.data.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KafkaEventType {
    CREATE("create"),
    UPDATE("update");

    private String label;
}
