package com.safa.logisticintegration.controllers;


import com.safa.logisticintegration.data.dto.shipment.LogisticProviderOutGoingDto;
import com.safa.logisticintegration.service.logisticProviders.commonProcessors.CommonLogisticProviderProcessor;
import io.micrometer.core.instrument.config.validate.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * Controller For logistic providers related apis.
 *
 */
@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class LogisticProvidersController {

    private final CommonLogisticProviderProcessor commonLogisticProviderProcessor;

    @Operation(summary = "Get all logistic providers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Authorization denied"),
            @ApiResponse(responseCode = "500", description = "Unexpected system exception")
    })
    @GetMapping("/v1/logistic-providers")
    public ResponseEntity<List<LogisticProviderOutGoingDto>> getLogisticProviders() {
        try {
            return ResponseEntity.ok(commonLogisticProviderProcessor.
                    getLogisticProvidersDto());
        } catch (ValidationException ex) {
            log.error("Validation error", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception ex) {
            log.error("Unexpected system exception", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
