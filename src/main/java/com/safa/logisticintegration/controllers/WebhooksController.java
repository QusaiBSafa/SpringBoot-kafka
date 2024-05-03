package com.safa.logisticintegration.controllers;


import com.safa.logisticintegration.common.LogisticProviderCodes;
import com.safa.logisticintegration.data.dto.tawseel.TawseelIncommingRequest;
import com.safa.logisticintegration.data.dto.transcorp.TranscorpOrderResponse;
import com.safa.logisticintegration.service.logisticProviders.ILogisticProviderProcessor;
import com.safa.logisticintegration.service.logisticProviders.LogisticProviderFactory;
import com.safa.logisticintegration.service.slack.SlackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.safa.logisticintegration.common.Constants.UNEXPECTED_ERROR;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * In this Controller will be all webhooks provided to logistic providers
 *
 */
@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class WebhooksController {

    private final LogisticProviderFactory logisticProviderFactory;

    private final SlackService slackService;

    @Operation(summary = "Update order by Transcorp webhook update request")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Authorization denied"),
                    @ApiResponse(responseCode = "500", description = "Unexpected system exception")
            })
    @PostMapping(value = {"/transcorp/v2", "/transcorp"})
    public ResponseEntity transcorpUpdateWebhook(HttpServletRequest request, @RequestBody TranscorpOrderResponse[] incomingRequest, @RequestHeader("clientSecret") String clientSecret) throws Exception {
        try {
            ILogisticProviderProcessor<TranscorpOrderResponse> logisticProvider = logisticProviderFactory.getInstance(LogisticProviderCodes.TRA.getCode());
            logisticProvider.updateShipment(clientSecret, incomingRequest[0]);
            return ResponseEntity.ok().build();

        } catch (Exception ex) {
            log.error(UNEXPECTED_ERROR, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UNEXPECTED_ERROR);
        }
    }

    @Operation(summary = "Update order by Tawseel webhook request")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "401", description = "Authorization denied"),
                    @ApiResponse(responseCode = "500", description = "Unexpected system exception")
            })
    @PostMapping("/v1/tawseel")
    public ResponseEntity tawseelUpdateWebhook(@RequestBody TawseelIncommingRequest incomingRequest, @RequestHeader("clientSecret") String clientSecret) throws Exception {
        try {
            var logisticProvider = logisticProviderFactory.getInstance(LogisticProviderCodes.WSL.getCode());
            logisticProvider.updateShipment(clientSecret, incomingRequest);
        } catch (Exception ex) {
            //TODO Remove the try catch here
            slackService.postErrorMessage(ex.getMessage());
        }
        return ResponseEntity.ok().build();
    }


}
