package com.safa.logisticintegration.client;

import com.safa.logisticintegration.data.dto.transcorp.TranscorpOrderRequest;
import com.safa.logisticintegration.data.dto.transcorp.TranscorpOrderResponse;
import com.safa.logisticintegration.data.dto.transcorp.TranscorpTokenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import static com.safa.logisticintegration.common.Constants.*;

@FeignClient(name = "transcorpClient", url = "${client.transcorp.baseUrl:}")
public interface TranscorpClient {


    @PostMapping("/api/auth/authenticate")
    TranscorpTokenDto generateTranscorpToken(@RequestHeader(CLIENT_ID) String clientId, @RequestParam(USERNAME) String username,
                                             @RequestParam(PASSWORD) String password);


    @PostMapping("/api/tasks")
    TranscorpOrderResponse sendCreateShipmentRequest(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader(CLIENT_ID) String clientId,
                                                     @RequestBody TranscorpOrderRequest shipmentRequestDto);

    @GetMapping("/api/tasks/{taskId}")
    TranscorpOrderResponse getShipmentsStatusRequest(@PathVariable(TASK_ID) Long taskId,
                                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader(CLIENT_ID) String clientId);


}
