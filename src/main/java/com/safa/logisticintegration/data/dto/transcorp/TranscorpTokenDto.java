package com.safa.logisticintegration.data.dto.transcorp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscorpTokenDto {

    private String accessToken;

    private String refreshToken;

    private String accessTokenExpiration;

    private String refreshTokenExpiration;

    private String email;

    private String name;

    private Object Role;

    private Long companyId;

    private Long userId;

    private String trackingItem;

    private String type;

    private String phoneNumber;
}


