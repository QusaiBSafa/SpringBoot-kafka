package com.safa.logisticintegration.data.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KafkaEventName {
    ORDER("order"),
    CONSULTATION("consultation");

    private final String name;
}
