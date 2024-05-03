package com.safa.logisticintegration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import static com.safa.logisticintegration.common.Constants.TOKEN;

/**
 * This to get the shipment label from tawseel
 * This API is created in interface alone because we don't want to apply the feign client configuration of json decoder on generate label api.
 */
@FeignClient(name = "tawseelClient", url = "${client.tawseel.baseUrl:https://logistics.dependo.com}")
public interface TawseelShipmentLabelClient {
    /**
     * Create shipment label from tawseel
     */
    @GetMapping("/index.php/api/print_dispatch_dpharma/dispatch_label_api/{apiKey}/{awbBillNo}")
    byte[] generateLabel(@RequestHeader(TOKEN) String token, @PathVariable String apiKey, @PathVariable String awbBillNo);

}
