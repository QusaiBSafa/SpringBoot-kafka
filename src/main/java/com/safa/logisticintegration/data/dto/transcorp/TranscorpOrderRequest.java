package com.safa.logisticintegration.data.dto.transcorp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request for create order/task to send to Transcorp api
 *
 * @Author Amneh
 */
@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscorpOrderRequest {

    @JsonProperty("customerId")
    private Long customerId;

    //  orderId
    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @JsonProperty("customerOrderNumber")
    private String customerOrderNumber;

    @JsonProperty("awb")
    private String awb; //orderId

    /**
     * Delivery details
     */
    @JsonProperty("deliveryDate")
    private String deliveryDate;

    @JsonProperty("deliveryStartTime")
    private String deliveryStartTime;

    @JsonProperty("deliveryEndTime")
    private String deliveryEndTime;

    @JsonProperty("type")
    private String type;

    @JsonProperty("deliveryType")
    private String deliveryType;

    //Represents transcorp at their service provider
    @JsonProperty("companyId")
    private int companyId;


    @JsonProperty("totalShipmentQuantity")
    private int totalShipmentQuantity;

    /**
     * Patient details
     */
    @JsonProperty("consignee")
    private Consignee consignee;

    @Setter
    @Getter
    public static class Consignee {
        @JsonProperty("name")
        private String name;

        @JsonProperty("location")
        private Location location;
    }

    @Setter
    @Getter
    public static class Location {
        @JsonProperty("addressLine1")
        private String addressLine1;

        @JsonProperty("district")
        private String district;

        @JsonProperty("city")
        private String city;

        @JsonProperty("countryCode")
        private String countryCode;

        @JsonProperty("contactPhone")
        private String contactPhone;

    }
}