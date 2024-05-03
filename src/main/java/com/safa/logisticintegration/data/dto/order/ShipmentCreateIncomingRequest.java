package com.safa.logisticintegration.data.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.safa.logisticintegration.data.dto.common.MoneyDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Mapping order/shipment logistics creation between BE and this service
 *
 * @Author Amneh
 */
@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipmentCreateIncomingRequest {


    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("shipmentId")
    private String shipmentId;

    @JsonProperty("customerId")
    private Long customerId;

    @JsonProperty("totalShipmentQuantity")
    private int totalShipmentQuantity;

    @JsonProperty("addressDetails")
    private Location addressDetails;

    @JsonProperty("recipient")
    private String recipient;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("deliveryStartTime")
    private Date deliveryStartTime;

    @JsonProperty("deliveryEndTime")
    private Date deliveryEndTime;

    @JsonProperty("outOfDeliveryAt")
    private Date outOfDeliveryAt;

    @JsonProperty("logisticProviderId")
    private Integer logisticProviderId;

    @JsonProperty("logisticProviderCode")
    private String logisticProviderCode;

    @JsonProperty("failedDeliveryAttemptReason")
    private String failedDeliveryAttemptReason;

    @JsonProperty("patientShare")
    private MoneyDto patientShare;

    @JsonProperty("totalPrice")
    private MoneyDto totalPrice;

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class Location {

        @JsonProperty("apartment")
        private String apartment;

        @JsonProperty("street")
        private String street;

        @JsonProperty("building")
        private String building;

        @JsonProperty("city")
        private String city;

        @JsonProperty("area")
        private String area;

        @JsonProperty("country")
        private String country;

    }

}
