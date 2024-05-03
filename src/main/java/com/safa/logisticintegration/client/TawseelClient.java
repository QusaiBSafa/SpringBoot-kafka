package com.safa.logisticintegration.client;


import com.safa.logisticintegration.data.dto.tawseel.TawseelBulkShipmentsRequestDto;
import com.safa.logisticintegration.data.dto.tawseel.TawseelBulkShipmentsResponse;
import com.safa.logisticintegration.data.dto.tawseel.TawseelCreateShipmentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import static com.safa.logisticintegration.common.Constants.REQ_ID;
import static com.safa.logisticintegration.common.Constants.TOKEN;

/**
 * @author Qusai Safa
 * FeignClient interface for sending http requests to tawseel third party.
 */
@FeignClient(name = "tawseelClient", url = "${client.tawseel.baseUrl:https://logistics.dependo.com}")
public interface TawseelClient {
    /**
     * Create tawseel shipment
     * TODO: change String response to TawseelCreateShipmentResponse, it is not workign now since they return the response with response type text,
     * We need to define custom decoder to map from string to TawseelCreateShipmentResponse
     */
    @PostMapping("/alltracking/create_shipment_dpharma/doUpload")
    String createShipment(@RequestHeader(TOKEN) String token, @RequestBody TawseelCreateShipmentRequest tawseelCreateShipmentRequest);

    /**
     * Get shipment from tawseel by awb
     */
    @GetMapping("/api/detailed_status_dpharma/trackAwb")
    TawseelBulkShipmentsResponse getShipment(@RequestHeader(TOKEN) String token, @RequestParam String awb, @RequestParam(REQ_ID) String uniqueRequestId);


    /**
     * Get Shipments from tawseel by awb list
     * This Get API is with body request based on Tawseel documentation
     */
    @GetMapping("/api/detailed_status_bulk_dpharma/track_bulk")
    String getBulkShipments(@RequestHeader(TOKEN) String token, @RequestHeader(REQ_ID) String uniqueRequestId, @RequestBody TawseelBulkShipmentsRequestDto tawseelBulkShipmentsRequestDto);

}
