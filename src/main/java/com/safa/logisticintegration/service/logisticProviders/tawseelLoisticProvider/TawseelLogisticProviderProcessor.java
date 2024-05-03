package com.safa.logisticintegration.service.logisticProviders.tawseelLoisticProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safa.logisticintegration.client.TawseelClient;
import com.safa.logisticintegration.client.TawseelShipmentLabelClient;
import com.safa.logisticintegration.common.LogisticProviderCodes;
import com.safa.logisticintegration.data.dto.order.ShipmentCreateIncomingRequest;
import com.safa.logisticintegration.data.dto.shipment.ShipmentOutgoingDto;
import com.safa.logisticintegration.data.dto.tawseel.*;
import com.safa.logisticintegration.data.entity.LogisticProvider;
import com.safa.logisticintegration.data.entity.ShipmentRequest;
import com.safa.logisticintegration.exception.BadRequestException;
import com.safa.logisticintegration.repository.LogisticProviderRepository;
import com.safa.logisticintegration.repository.ShipmentRequestRepository;
import com.safa.logisticintegration.service.kafka.KafkaProducerService;
import com.safa.logisticintegration.service.logisticProviders.ILogisticProviderProcessor;
import com.safa.logisticintegration.service.logisticProviders.commonProcessors.CommonLogisticProviderProcessor;
import com.safa.logisticintegration.service.slack.SlackService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.safa.logisticintegration.common.Utils.generateSHA512Hash;

/**
 * @author Qusai Safa
 * This processor responsible for processing requests and responses between alma health and tawseel third party.
 */
@Component

public class TawseelLogisticProviderProcessor extends CommonLogisticProviderProcessor implements ILogisticProviderProcessor<TawseelIncommingRequest> {

    public static final String ERROR_PULL_SHIPMENT_UPDATE_STATUS = "Pull shipment error: Failed updating status for shipment with id: %s, Tawseel incoming request: %s, error message: %s";
    private final TawseelClient tawseelClient;
    private final TawseelShipmentLabelClient tawseelShipmentLabelClient;
    private final TawseelTransformer tawseelTransformer;

    /**
     * Workaround, tawseel bulk shipments api use one api key for both accounts on dev and prod
     */
    @Value("${tawseel.bulk.apiKey:18030}")
    private String tawseelBulkShipmentsApiKey;

    private final Logger logger = LoggerFactory.getLogger(TawseelLogisticProviderProcessor.class);

    @Autowired
    public TawseelLogisticProviderProcessor(ObjectMapper objectMapper, SlackService slackService, ShipmentRequestRepository shipmentRequestRepository, LogisticProviderRepository logisticProviderRepository, KafkaProducerService kafkaProducerService, TawseelClient tawseelClient, TawseelShipmentLabelClient tawseelShipmentLabelClient, TawseelTransformer tawseelTransformer) {
        super(objectMapper, slackService, shipmentRequestRepository, logisticProviderRepository, kafkaProducerService);
        this.tawseelClient = tawseelClient;
        this.tawseelShipmentLabelClient = tawseelShipmentLabelClient;
        this.tawseelTransformer = tawseelTransformer;
    }

    @Override
    public ShipmentOutgoingDto createShipment(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest) throws Exception {
        // Drop duplicate requests from same shipment
        isValidIncomingRequest(shipmentCreateIncomingRequest);
        // Get logistic provider
        var logisticProvider = getLogisticProvidersById(shipmentCreateIncomingRequest.getLogisticProviderId());
        // Create request to Tawseel
        if (logisticProvider.isCreateShipmentEnabled()) {
            TawseelCreateShipmentRequest tawseelCreateShipmentRequest = tawseelTransformer.transformToThirdPartyRequest(shipmentCreateIncomingRequest, logisticProvider);
            // Create token
            String token = createTokenForShipmentCreationRequest(logisticProvider.getAccountId(), tawseelCreateShipmentRequest.getOrderNo(), logisticProvider.getUsername(), logisticProvider.getPassword());
            // Send request to Tawseel
            String response = tawseelClient.createShipment(token, tawseelCreateShipmentRequest);
            TawseelCreateShipmentResponse tawseelCreateShipmentResponse = objectMapper.readValue(response, TawseelCreateShipmentResponse.class);
            // Validate response
            validateResponse(shipmentCreateIncomingRequest, tawseelCreateShipmentResponse);
            // Save the shipment after it is successfully created in Tawseel
            return saveShipmentRequest(shipmentCreateIncomingRequest, true);
        }
        // Save shipment with no tawseel creation and isSentSuccessfullyToThirdParty is false
        return saveShipmentRequest(shipmentCreateIncomingRequest, false);
    }

    /**
     * Validate tawseel response
     */
    private static void validateResponse(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest, TawseelCreateShipmentResponse tawseelCreateShipmentResponse) {
        if (tawseelCreateShipmentResponse == null || CollectionUtils.isEmpty(tawseelCreateShipmentResponse.getResponse())) {
            throw new BadRequestException(String.format("Error: creating shipment failed, orderId : %s", shipmentCreateIncomingRequest.getShipmentId()));
        }
        String error = tawseelCreateShipmentResponse.getResponse().get(0).getError();
        if (StringUtils.isNotEmpty(error)) {
            throw new BadRequestException(String.format("Error: creating shipment failed, orderId : %s, message : %s", shipmentCreateIncomingRequest.getShipmentId(), error));
        }
    }

    @Override
    public void updateShipment(String accessToken, TawseelIncommingRequest incomingRequest) throws Exception {
        validateUpdateShipmentToken(accessToken, incomingRequest.toString());
        updateShipment(incomingRequest);
    }

    private void updateShipment(TawseelIncommingRequest incomingRequest) throws Exception {
        // Check reference number
        String referenceNumber = getReferenceNumber(incomingRequest);
        // Get Tawseel status
        String externalStatus = incomingRequest.getStatus();

        String externalStatusDescription = incomingRequest.getStatusDescription();
        // Update shipment request
        updateShipmentRequest(referenceNumber, externalStatus, tawseelTransformer.mapStatus(externalStatus), externalStatusDescription);
    }

    private String getReferenceNumber(TawseelIncommingRequest incomingRequest) {
        if (StringUtils.isNotEmpty(incomingRequest.getOrderNumber())) {
            return incomingRequest.getOrderNumber();
        } else if (StringUtils.isNotEmpty(incomingRequest.getAwbNumber())) {
            return getShipmentByAwbNo(incomingRequest.getAwbNumber()).getReferenceId();
        } else {
            throw new BadRequestException(String.format("Error: shipment request update, order number and awb number were not set in the request from tawseel: %s", incomingRequest));
        }
    }

    /**
     * Pull shipments from tawseel, this is helpful in case the webhook update is not working for some reason
     * Get tawseel accounts by calling this method {@link this.getLogisticProvidersWithEnabledPullShipments}
     * Get active shipment requests
     * Send get bulk shipments to tawseel
     * Compare shipments statuses from tawseel side and our side and do update if needed.
     * Send report to slack
     *
     * @param token
     */
    @Override
    @Async
    public void pullShipments(String token) {
        validatePullShipmentsToken(token);

        // Get logistic providers with is pull shipments true
        Set<LogisticProvider> logisticProviders = getLogisticProvidersWithEnabledPullShipments(LogisticProviderCodes.WSL);

        List<String> updatedShipmentsIds = new ArrayList<>();
        // Each logistic provider might have multiple accounts, loop over all acounts
        for (LogisticProvider logisticProvider : logisticProviders) {
            // Get shipments and map to tawseel TawseelBulkShipmentsRequestDto.AirwayBil
            Set<ShipmentRequest> shipmentRequests = getActiveShipmentsByLogisticProviders(List.of((int) logisticProvider.getId()));
            // If there are active shipments
            if (CollectionUtils.isNotEmpty(shipmentRequests)) {
                // Map shipment to map of awb and shipment request
                Map<String, ShipmentRequest> shipmentRequestsMap = shipmentRequests.stream().collect(Collectors.toMap(ShipmentRequest::getAwb, shipmentRequest -> shipmentRequest));
                // Create tawseel get bulk shipments request
                TawseelBulkShipmentsRequestDto tawseelBulkShipmentsRequestDto = TawseelBulkShipmentsRequestDto.builder().airwayBilNo(shipmentRequestsMap.keySet().stream().map(TawseelBulkShipmentsRequestDto.AirwayBil::new).toList()).build();
                // Create tawseel token
                String uuid = UUID.randomUUID().toString();
                String tawseelToken = createTokenForGetShipmentsDetails(tawseelBulkShipmentsApiKey, uuid, logisticProvider.getPassword());
                // Send get bulk shipments request
                TawseelBulkShipmentsResponse tawseelBulkShipmentsResponse;
                String response = null;
                Map<String, TawseelIncommingRequest> shipmentResponseMap = new HashMap<>();
                try {
                    // For bulk shipments request the apiKey is the same apiKEy used for dev and prod.
                    response = tawseelClient.getBulkShipments(tawseelToken, uuid, tawseelBulkShipmentsRequestDto);
                    tawseelBulkShipmentsResponse = objectMapper.readValue(response, TawseelBulkShipmentsResponse.class);
                    // GetBulkShipments return the full history of the shipment, so we need to loop over it and get the last status only
                    for (TawseelIncommingRequest tawseelIncommingRequest : tawseelBulkShipmentsResponse.getScanDetail()) {
                        if (shipmentResponseMap.containsKey(tawseelIncommingRequest.getOrderNumber())) {
                            if (shipmentResponseMap.get(tawseelIncommingRequest.getOrderNumber()).getUpdatedDate().before(tawseelIncommingRequest.getUpdatedDate())) {
                                shipmentResponseMap.put(tawseelIncommingRequest.getOrderNumber(), tawseelIncommingRequest);
                            }
                        } else {
                            shipmentResponseMap.put(tawseelIncommingRequest.getOrderNumber(), tawseelIncommingRequest);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    this.slackService.postErrorMessage(String.format("Failed pull shipments from tawseel using bulk shipments request, requestDto: %s, tawseel token: %s, tawseel response: %s, error message: %s", tawseelBulkShipmentsRequestDto, tawseelToken, response, e.getMessage()));
                    return;
                }

                // If response is not nul or empty
                if (CollectionUtils.isNotEmpty(tawseelBulkShipmentsResponse.getScanDetail())) {
                    for (TawseelIncommingRequest tawseelIncommingRequest : shipmentResponseMap.values()) {
                        // Get shipment request by AWB
                        ShipmentRequest shipmentRequest = shipmentRequestsMap.get(tawseelIncommingRequest.getAwbNumber());
                        // Check status if different than saved status then update the shipment
                        String externalStatus = tawseelIncommingRequest.getStatus();
                        if (StringUtils.isNotEmpty(externalStatus) && !externalStatus.equals(shipmentRequest.getExternalStatus())) {
                            // Update shipment
                            try {
                                updateShipment(tawseelIncommingRequest);
                            } catch (Exception e) {
                                slackService.postErrorMessage(String.format(ERROR_PULL_SHIPMENT_UPDATE_STATUS, shipmentRequest.getReferenceId(), tawseelIncommingRequest, e.getMessage()));
                            }
                            updatedShipmentsIds.add(shipmentRequest.getReferenceId());
                        }
                    }
                }

            }
        }
        sendPullShipmentSummaryToSlack(LogisticProviderCodes.WSL, updatedShipmentsIds);
    }

    @Override
    public byte[] getShipmentLabel(ShipmentRequest shipmentRequest) {
        LogisticProvider logisticProvider = getLogisticProvidersById(shipmentRequest.getLogisticProviderId());
        String token = createTokenForGetShipmentLabel(logisticProvider.getAccountId(), shipmentRequest.getAwb(), logisticProvider.getPassword());
        return tawseelShipmentLabelClient.generateLabel(token, logisticProvider.getAccountId(), shipmentRequest.getAwb());
    }


    @Override
    public String getCode() {
        return LogisticProviderCodes.WSL.getCode();
    }

    /**
     * token for create shipment  = api_key+order_no+vendorname+secret_key
     * apiKey = logistic provider account id
     * vendorName = logistic provider username
     * secretKey = logistic provider password
     */
    private String createTokenForShipmentCreationRequest(String apiKey, String orderId, String vendorName, String secretKey) {
        String tokenString = String.format("%s%s%s%s", apiKey, orderId, vendorName, secretKey);
        return generateSHA512Hash(tokenString);
    }


    /**
     * Token for shipment label
     * APIKey + awbno + secret_key
     */
    private String createTokenForGetShipmentLabel(String apiKey, String awbNo, String secretKey) {
        String tokenString = String.format("%s%s%s", apiKey, awbNo, secretKey);
        return generateSHA512Hash(tokenString);
    }


    /**
     * Create Token for get multiple shipments (bulk tracking)
     * APIKey + uniq_request_id + secret_key
     */
    private String createTokenForGetShipmentsDetails(String apiKey, String uniqueRequestId, String secretKey) {
        String tokenString = String.format("%s%s%s", apiKey, uniqueRequestId, secretKey);
        return generateSHA512Hash(tokenString);
    }

}
