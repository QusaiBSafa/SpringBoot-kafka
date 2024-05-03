package com.safa.logisticintegration.data.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KafkaEventSource {
    THIRD_PARTY_LOGISTIC_CREATE("Third party logistic create"),
    THIRD_PARTY_LOGISTIC_UPDATE("Third party logistic update"),
    THIRD_PARTY_ELIGIBILITY_CHECK_UPDATE("Third party eligibility check update");

    private String source;
}
