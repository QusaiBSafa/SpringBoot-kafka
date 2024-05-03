package com.safa.logisticintegration.data.dto.tawseel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for tawseel create shipment request
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TawseelCreateShipmentResponse {

    @JsonProperty("response")
    private List<Response> response;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        private String success;

        @JsonProperty("awbno")
        private String awbNo;

        @JsonProperty("Courier")
        private String courier;

        private String error;
    }
}



