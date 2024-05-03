package com.safa.logisticintegration.data.dto.transcorp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Request for creating orders send by Transcorp
 *
 * @Author Amneh
 */
@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranscorpOrderResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("awb")
    private String awb;
    @JsonProperty("deliveryInformation")
    private DeliveryInformation deliveryInformation;
    @JsonProperty("customer")
    private Customer customer;

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryInformation {
        @JsonProperty("failureReasonComment")
        private String failureReasonComment;

        @JsonProperty("deliveryDate")
        private Date deliveryDate;

        @JsonProperty("numberOfAttempts")
        private int numberOfAttempts;

    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Customer {

        @JsonProperty("id")
        private Long id;
    }

}