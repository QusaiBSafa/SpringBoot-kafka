package com.safa.logisticintegration.service.logisticProviders.transcorpLogisticProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safa.logisticintegration.client.TranscorpClient;
import com.safa.logisticintegration.client.TranscorpShipmentLabel;
import com.safa.logisticintegration.common.LogisticProviderCodes;
import com.safa.logisticintegration.data.dto.order.ShipmentCreateIncomingRequest;
import com.safa.logisticintegration.data.dto.shipment.ShipmentOutgoingDto;
import com.safa.logisticintegration.data.dto.transcorp.TranscorpOrderRequest;
import com.safa.logisticintegration.data.dto.transcorp.TranscorpOrderResponse;
import com.safa.logisticintegration.data.dto.transcorp.TranscorpTokenDto;
import com.safa.logisticintegration.data.entity.ShipmentRequest;
import com.safa.logisticintegration.repository.LogisticProviderRepository;
import com.safa.logisticintegration.repository.ShipmentRequestRepository;
import com.safa.logisticintegration.service.kafka.KafkaProducerService;
import com.safa.logisticintegration.service.logisticProviders.ILogisticProviderProcessor;
import com.safa.logisticintegration.service.logisticProviders.commonProcessors.CommonLogisticProviderProcessor;
import com.safa.logisticintegration.service.slack.SlackService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TranscorpLogisticProviderProcessor extends CommonLogisticProviderProcessor implements ILogisticProviderProcessor<TranscorpOrderResponse> {

    private final TranscorpClient transcorpClient;
    private final TranscorpShipmentLabel transcorpShipmentLabel;
    private final TranscorpTransformer transcorpTransformer;

    @Value("${transcorp.clientId}")
    private String clientId;

    @Autowired
    public TranscorpLogisticProviderProcessor(ObjectMapper objectMapper, SlackService slackService, ShipmentRequestRepository shipmentRequestRepository, LogisticProviderRepository logisticProviderRepository, KafkaProducerService kafkaProducerService, TranscorpClient transcorpClient, TranscorpShipmentLabel transcorpShipmentLabel, TranscorpTransformer transcorpTransformer) {
        super(objectMapper, slackService, shipmentRequestRepository, logisticProviderRepository, kafkaProducerService);
        this.transcorpClient = transcorpClient;
        this.transcorpShipmentLabel = transcorpShipmentLabel;
        this.transcorpTransformer = transcorpTransformer;
    }

    @Override
    public ShipmentOutgoingDto createShipment(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest) throws Exception {

        // Drop duplicate requests from same shipment
        isValidIncomingRequest(shipmentCreateIncomingRequest);
        // is the delivery address valid for transcorp, this means the delivery time needs to be in future
        isDeliveryStartTimeInFuture(shipmentCreateIncomingRequest.getDeliveryStartTime(), shipmentCreateIncomingRequest.getShipmentId());

        var logisticProvider = getLogisticProvidersById(shipmentCreateIncomingRequest.getLogisticProviderId());

        if (logisticProvider.isCreateShipmentEnabled()) {
            // Create token
            String authToken = generateAccessToken(logisticProvider.getId(), clientId, logisticProvider.getUsername(),
                    logisticProvider.getPassword());
            // Transform to third party request
            TranscorpOrderRequest transcorpOrderRequest = transcorpTransformer.transformToThirdPartyRequest(shipmentCreateIncomingRequest, logisticProvider);
            // Send request and get response
            TranscorpOrderResponse transcorpResponse = this.transcorpClient.sendCreateShipmentRequest(
                    authToken, clientId, transcorpOrderRequest);
            // Create and init the shipment request and save it in our DB, and send the updates to the original order in other services using kafka events.
            return saveShipmentRequest(shipmentCreateIncomingRequest, transcorpResponse.getStatus(), transcorpTransformer.mapStatus(transcorpResponse.getStatus()), transcorpResponse.getId(), true);

        }
        // In case isCreateShipmentEnabled is not enabled then save with isSentSuccessfully = false
        return saveShipmentRequest(shipmentCreateIncomingRequest, false);
    }


    @Override
    public void updateShipment(String accessToken, TranscorpOrderResponse transcorpResponse) throws Exception {
        validateUpdateShipmentToken(accessToken, transcorpResponse.toString());
        log.info("Webhook update request" + transcorpResponse);
        updateShipment(transcorpResponse);
    }

    /**
     * Update shipment by extracting changed data from transcorp
     */
    private ShipmentOutgoingDto updateShipment(TranscorpOrderResponse transcorpResponse) throws Exception {
        String referenceNumber = getReferenceNumber(transcorpResponse);
        String mappedStatus = transcorpTransformer.mapStatus(transcorpResponse.getStatus());
        return updateShipmentRequest(referenceNumber, transcorpResponse.getStatus(), mappedStatus, null);
    }

    /**
     * Get reference number with multiple levels of validation
     */
    private String getReferenceNumber(TranscorpOrderResponse transcorpResponse) {
        //Level1: validate on reference number
        if (transcorpResponse.getReferenceNumber() != null) {
            return transcorpResponse.getReferenceNumber();
            //Level2: Validate on external task Id
        } else if (transcorpResponse.getId() != null) {
            log.info("transcorpResponse.getReferenceNumber() returned null, {}", transcorpResponse);
            ShipmentRequest shipmentRequest = shipmentRequestRepository.findByExternalTaskId(transcorpResponse.getId());
            if (shipmentRequest != null && shipmentRequest.getReferenceId() != null) {
                return shipmentRequest.getReferenceId();
                //Level3: Validate on AWB in case transcorp returned a new external taskID for the same shipment
            } else if (transcorpResponse.getAwb() != null) {
                log.info("shipmentRequest.getReferenceNumber() returned null, {}", shipmentRequest);
                shipmentRequest = shipmentRequestRepository.findByAwb(transcorpResponse.getAwb());
                if (shipmentRequest != null && shipmentRequest.getReferenceId() != null) {
                    return shipmentRequest.getReferenceId();
                } else {
                    log.error("Unable to find a matching shipment request for AWB: {}", transcorpResponse.getAwb());
                    throw new EntityNotFoundException("Unable to find a matching shipment request for AWB");
                }
            }
            //Level4: Validate on AWB in case transcorp didn't return taskId at all
        } else if (transcorpResponse.getAwb() != null) {
            ShipmentRequest shipmentRequest = shipmentRequestRepository.findByAwb(transcorpResponse.getAwb());
            if (shipmentRequest != null && shipmentRequest.getReferenceId() != null) {
                return shipmentRequest.getReferenceId();
            } else {
                log.error("Unable to find a matching shipment request for AWB: {}", transcorpResponse.getAwb());
                throw new EntityNotFoundException("Unable to find a matching shipment request for AWB");
            }
        } else {
            log.error("Unable to determine the reference number from transcorpResponse: {}", transcorpResponse);
            throw new EntityNotFoundException("Unable to determine the reference number");
        }
        return null;
    }

    @Override
    public void pullShipments(String token) {
        return;
//        // Check token
//        validatePullShipmentsToken(token);
//
//        // Get logistic providers with is pull shipments true
//        Set<LogisticProvider> logisticProviders = this.getLogisticProvidersWithEnabledPullShipments(LogisticProviderCodes.TRA);
//
//        if (CollectionUtils.isEmpty(logisticProviders)) {
//            throw new EntityNotFoundException(String.format("Error: Logistic providers are not set in DB for %s, code %s", LogisticProviderCodes.TRA.getLogisticProviderName(), LogisticProviderCodes.TRA.getCode()));
//        }
//
//        // Get active shipment for logistic providers with is pull shipments enabled
//        Set<ShipmentRequest> activeShipments = this.getActiveShipmentsByLogisticProviders(logisticProviders.stream().map(logisticProvider -> (int) (logisticProvider.getId())).toList());
//
//        if (CollectionUtils.isEmpty(activeShipments)) {
//            sendPullShipmentSummaryToSlack(LogisticProviderCodes.TRA, Collections.emptyList());
//            return;
//        }
//        // Fill Map with (key:logisticId and value:authToken) to get each provider auth next.
//        Map<Long, String> logisticAuthTokens = new HashMap<>();
//        for (LogisticProvider logisticProvider : logisticProviders) {
//            String authToken = generateAccessToken(logisticProvider.getId(), clientId, logisticProvider.getUsername(),
//                    logisticProvider.getPassword());
//            logisticAuthTokens.put(logisticProvider.getId(), authToken);
//        }
//
//        List<String> transcorpUpdatedOrders = activeShipments.stream()
//                .filter(shipment -> shipment.getLogisticProviderId() != null
//                        && logisticAuthTokens.containsKey(Long.valueOf(shipment.getLogisticProviderId()))
//                        // Special case in transcorp they return externalTaskId value 0 when the
//                        // Same shipment is recreated again.
//                        && shipment.getExternalTaskId() != 0)
//                .map(shipment -> {
//                    // Get status from Transcorp api.
//                    TranscorpOrderResponse transcorpStatusResponse = transcorpClient.getShipmentsStatusRequest(
//                            shipment.getExternalTaskId(), logisticAuthTokens.get(Long.valueOf(shipment.getLogisticProviderId())), clientId);
//                    // Check if the both statues are the same then don't need to update and continue to next status.
//                    // If statues are not the same store them in the array of orders with new statuses to update them.
//                    if (transcorpStatusResponse != null && StringUtils.isNotEmpty(transcorpStatusResponse.getStatus()) &&
//                            !transcorpStatusResponse.getStatus().equals(shipment.getExternalStatus())
//                    ) {
//                        // Update the existing shipment with the new status
//                        try {
//                            return updateShipment(transcorpStatusResponse);
//                        } catch (Exception ex) {
//                            // Send error to slack and continue updating other shipments
//                            // We don't throw exception here to allow the process to continue updating other shipments
//                            slackService.postErrorMessage(String.format("Failed while updating shipment(orderId: %s) using pull shipment cron job request", shipment.getReferenceId()));
//                        }
//                    }
//                    return null;
//                })
//                .filter(Objects::nonNull).map(ShipmentOutgoingDto::getReferenceId).toList();
//
//        sendPullShipmentSummaryToSlack(LogisticProviderCodes.TRA, transcorpUpdatedOrders);
    }

    @Override
    public byte[] getShipmentLabel(ShipmentRequest shipmentRequest) {
        var logisticProvider = getLogisticProvidersById(shipmentRequest.getLogisticProviderId());
        String authToken = generateAccessToken(shipmentRequest.getLogisticProviderId(), clientId, logisticProvider.getUsername(),
                logisticProvider.getPassword());
        return this.transcorpShipmentLabel.getShipmentLabel(clientId, authToken, shipmentRequest.getExternalTaskId()).getBody();
    }

    private String generateAccessToken(long logisticProviderId, String clientId, String username, String password) {
        try {
            TranscorpTokenDto tokenDto = transcorpClient.generateTranscorpToken(clientId, username, password);
            return tokenDto.getAccessToken();
        } catch (Exception ex) {
            // Since we are creating token for multiple logistic providers, we need to log the error only without throwing exception to keep the process of updating running for other accounts
            slackService.postErrorMessage(String.format("Error: Failed creating token for trancorp logistic provider id %s, error message : %s", logisticProviderId, ex.getMessage()));
            return null;
        }
    }

    @Override
    public String getCode() {
        return LogisticProviderCodes.TRA.getCode();
    }

}
