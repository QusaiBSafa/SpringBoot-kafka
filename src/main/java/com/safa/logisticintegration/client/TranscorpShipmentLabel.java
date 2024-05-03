package com.safa.logisticintegration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.safa.logisticintegration.common.Constants.TASK_ID;
import static com.safa.logisticintegration.common.Constants.TOKEN;

@FeignClient(name = "transcorpClient", url = "${client.transcorp.shipmentUrl}")
public interface TranscorpShipmentLabel {

    @GetMapping("/generate-label")
    ResponseEntity<byte[]> getShipmentLabel(@RequestParam String clientId,
                                            @RequestParam(TOKEN) String token, @RequestParam(TASK_ID) Long taskId);

}
