package com.safa.logisticintegration.data.dto.shipment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentOutgoingDto {

    @JsonProperty
    private String referenceId;

    @JsonProperty
    private long externalTaskId;

    @JsonProperty
    private Date outOfDeliveryAt;

    @JsonProperty
    private String failedDeliveryAttemptReason;

    @JsonProperty
    private String totalShipmentQuantity;

    @JsonProperty
    private String recipient;

    @JsonProperty
    private String phoneNumber;

    @JsonProperty
    private Date deliveryStartTime;

    @JsonProperty
    private Date deliveryEndTime;

    @JsonProperty
    private String status;

    @JsonProperty
    private Integer logisticProviderId;

    @JsonProperty
    private String awb;

    @JsonProperty
    private String logisticProviderCode;

    @JsonProperty
    private Date createdAt;

    @JsonProperty
    private Date updatedAt;

    @JsonProperty
    private boolean deleted;

    @JsonProperty
    private String street;

    @JsonProperty
    private String apartment;

    @JsonProperty
    private String building;

    @JsonProperty
    private String city;

    @JsonProperty
    private String area;

}
