package com.safa.logisticintegration.data.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogisticProvider {

    TRANSCORP("transcorp");

    private final String provider;
}
