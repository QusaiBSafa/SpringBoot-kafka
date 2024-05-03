package com.safa.logisticintegration.data.dto.tawseel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for tawseel get shipment status request
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TawseelBulkShipmentsResponse {

    @JsonProperty("scan_detail")
    private List<TawseelIncommingRequest> scanDetail;

    @JsonProperty("response")
    private List<Response> response;

    @Data
    @NoArgsConstructor
    public class Response {
        @JsonProperty("error")
        private String error;
    }

}
