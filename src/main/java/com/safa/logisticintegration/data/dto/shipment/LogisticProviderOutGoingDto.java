package com.safa.logisticintegration.data.dto.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogisticProviderOutGoingDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String name;
    
    @JsonProperty
    private String accountId;

    @JsonProperty
    private Long customerId;

    @JsonProperty
    private String logisticProviderCode;

    @JsonProperty
    private boolean isPullShipments;

}
