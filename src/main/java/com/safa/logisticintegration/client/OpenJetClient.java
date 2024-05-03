package com.safa.logisticintegration.client;

import com.safa.logisticintegration.data.dto.elgibility.openJet.eligibilityCheck.EligibilityCheckRequest;
import com.safa.logisticintegration.data.dto.elgibility.openJet.eligibilityCheck.EligibilityCheckResponse;
import com.safa.logisticintegration.data.dto.elgibility.openJet.eligibilityStatus.EligibilityStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "openJetClient", url = "${client.openJet.baseUrl}")
public interface OpenJetClient {

    @PostMapping("/addeligibilityrequest")
    EligibilityCheckResponse createEligibilityCheckRequest(@RequestHeader(HttpHeaders.AUTHORIZATION) String apiKey, EligibilityCheckRequest eligibilityCheckRequest);


    @GetMapping("/geteligibilityrequestdetailsbyeligibilityId")
    EligibilityStatusResponse getEligibilityStatusCheck(@RequestHeader(HttpHeaders.AUTHORIZATION) String apiKey, @RequestParam("eligibilityID") long eligibilityID);


    @PostMapping("/canceleligibilitycheckrequest")
    Object cancelEligibilityCheck(@RequestHeader(HttpHeaders.AUTHORIZATION) String apiKey, @RequestParam("eligibilityId") long eligibilityID);
}
