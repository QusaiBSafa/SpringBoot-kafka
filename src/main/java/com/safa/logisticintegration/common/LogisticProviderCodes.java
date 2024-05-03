package com.safa.logisticintegration.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogisticProviderCodes {

    //Internal
    AHL("AHL", "Internal Logistic Provider"),
    //Transcorp
    TRA("TRA", "Transcorp"),

    //Tawseel
    WSL("WSL", "Tawseel");


    private final String code;

    private final String logisticProviderName;
}
