package com.safa.logisticintegration.data.dto.tawseel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TawseelBulkShipmentsRequestDto {

    @JsonProperty("airwaybilno")
    private List<AirwayBil> airwayBilNo;

    @Data
    @AllArgsConstructor
    public static class AirwayBil {
        private String awb;
    }

}
