package com.safa.logisticintegration.service.logisticProviders.commonProcessors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safa.logisticintegration.common.LogisticProviderCodes;
import com.safa.logisticintegration.common.Utils;
import com.safa.logisticintegration.data.dto.kafka.KafkaEventSource;
import com.safa.logisticintegration.data.dto.order.OrderDto;
import com.safa.logisticintegration.data.dto.order.OrderStatus;
import com.safa.logisticintegration.data.dto.order.OrderUpdateOutgoingDto;
import com.safa.logisticintegration.data.dto.order.ShipmentCreateIncomingRequest;
import com.safa.logisticintegration.data.dto.shipment.LogisticProviderOutGoingDto;
import com.safa.logisticintegration.data.dto.shipment.ShipmentOutgoingDto;
import com.safa.logisticintegration.data.entity.LogisticProvider;
import com.safa.logisticintegration.data.entity.ShipmentRequest;
import com.safa.logisticintegration.exception.BadRequestException;
import com.safa.logisticintegration.exception.UnauthorizedException;
import com.safa.logisticintegration.repository.LogisticProviderRepository;
import com.safa.logisticintegration.repository.ShipmentRequestRepository;
import com.safa.logisticintegration.service.kafka.KafkaProducerService;
import com.safa.logisticintegration.service.slack.SlackService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.safa.logisticintegration.common.Constants.*;

/**
 * A common processor has a common logic needed to be extended to each Logistic Provider Processor
 */
@Slf4j
@Service
public class CommonLogisticProviderProcessor {

    protected final ObjectMapper objectMapper;
    protected final SlackService slackService;
    protected final ShipmentRequestRepository shipmentRequestRepository;
    private final LogisticProviderRepository logisticProviderRepository;
    protected final KafkaProducerService kafkaProducerService;

    @Value("${isr.access-token}")
    private String integrationStatusReaderAccessToken;

    @Value("${transcorp.access-token}")
    protected String updateShipmentAccessToken;

    @Autowired
    public CommonLogisticProviderProcessor(ObjectMapper objectMapper, SlackService slackService, ShipmentRequestRepository shipmentRequestRepository, LogisticProviderRepository logisticProviderRepository, KafkaProducerService kafkaProducerService) {
        this.objectMapper = objectMapper;
        this.slackService = slackService;
        this.shipmentRequestRepository = shipmentRequestRepository;
        this.logisticProviderRepository = logisticProviderRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    /**
     * Validate logistic providers incoming requests and add any other needed future validation.
     */
    protected void isValidIncomingRequest(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest) {
        String shipmentId = shipmentCreateIncomingRequest.getShipmentId();
        if (shipmentRequestRepository.findByReferenceId(shipmentId) != null) {
            throw new BadRequestException(String.format("Error: Duplicate shipment, shipment already created for this order (%s)", shipmentId));
        }

    }

    /**
     * Check if the delivery start time is in future
     */
    protected void isDeliveryStartTimeInFuture(Date deliveryStartTime, String orderId) {
        // Current date without time
        LocalDate currentDate = LocalDate.now();
        // convert delivery start time to date without time
        LocalDate deliveryStartTimeLocalDate = Utils.convertToLocalDate(deliveryStartTime);
        // Compare the two dates, deliveryStartTimeLocalDate should be in the future, otherwise throw exception
        if (deliveryStartTimeLocalDate.isEqual(currentDate) || deliveryStartTimeLocalDate.isBefore(currentDate)) {
            throw new BadRequestException(String.format("Delivery date cannot be in the past or present, please use future date, orderId %s", orderId));
        }
    }

    /**
     * Save logistic providers ShipmentRequest.
     */
    protected ShipmentRequest mapToShipmentRequest(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest,
                                                   String externalStatus, String status, Long externalId, boolean isSentSuccessfullyToThirdParty) {
        // Save all requested data on the database
        ShipmentCreateIncomingRequest.Location addressDetails = shipmentCreateIncomingRequest.getAddressDetails();
        return ShipmentRequest.builder().createdBy(shipmentCreateIncomingRequest.getCreatedBy())
                .referenceId(shipmentCreateIncomingRequest.getShipmentId())
                .externalTaskId(externalId != null ? externalId : 0)
                .outOfDeliveryAt(shipmentCreateIncomingRequest.getOutOfDeliveryAt())
                .failedDeliveryAttemptReason(shipmentCreateIncomingRequest.getFailedDeliveryAttemptReason())
                .totalShipmentQuantity(shipmentCreateIncomingRequest.getTotalShipmentQuantity() + "")
                .recipient(shipmentCreateIncomingRequest.getRecipient())
                .phoneNumber(shipmentCreateIncomingRequest.getPhoneNumber())
                .deliveryStartTime(shipmentCreateIncomingRequest.getDeliveryStartTime())
                .deliveryEndTime(shipmentCreateIncomingRequest.getDeliveryEndTime())
                .status(status != null ? status : OrderStatus.NEW_ORDER.getStatus())
                .externalStatus(externalStatus)
                .logisticProviderId(shipmentCreateIncomingRequest.getLogisticProviderId())
                .awb(AWB_PREFIX.concat(shipmentCreateIncomingRequest.getShipmentId()))
                .logisticProviderCode(shipmentCreateIncomingRequest.getLogisticProviderCode())
                .street(addressDetails.getStreet())
                .apartment(addressDetails.getApartment())
                .building(addressDetails.getBuilding())
                .city(addressDetails.getCity())
                .isSentSuccessfullyToThirdParty(isSentSuccessfullyToThirdParty)
                .area(addressDetails.getArea()).build();
    }

    /**
     * Get all existing already shipments stored for 2 weeks and still active
     */
    protected Set<ShipmentRequest> getActiveShipmentsByLogisticProviders(List<Integer> logisticProviderIds) {
        // 1 day before
        LocalDateTime endDate = LocalDateTime.now();
        // 2 weeks ago
        LocalDateTime startDate = endDate.minusDays(200);

        // Don't filter on these statuses as they are considered as a final status.
        List<String> excludedStatuses = List.of(
                OrderStatus.DELIVERED.getStatus(),
                OrderStatus.FAILED_DELIVERY_ATTEMPT.getStatus(),
                OrderStatus.CANCELLED.getStatus()
        );

        // Fetch the statuses for orders created in a range of (1 day before -2 weeks ago).
        return shipmentRequestRepository
                .findAllByCreatedAtBetweenAndIsSentSuccessfullyToThirdPartyTrueAndStatusNotInAndLogisticProviderIdIn(startDate, endDate, excludedStatuses, logisticProviderIds);
    }


    /**
     * Find all logistic providers from DB and return them.
     *
     * @return List<LogisticProvider>
     */
    public List<LogisticProvider> getLogisticProviders() {
        // Find all logistic providers from DB and return them.
        List<LogisticProvider> listOfAllLogisticProviders = new ArrayList<>();
        logisticProviderRepository.findAll().forEach(listOfAllLogisticProviders::add);
        return listOfAllLogisticProviders;
    }

    /**
     * Find all logistic providers from DB and return them.
     *
     * @return List<LogisticProviderOutGoingDto>
     */
    public List<LogisticProviderOutGoingDto> getLogisticProvidersDto() {
        List<LogisticProvider> listOfAllLogisticProviders = getLogisticProviders();
        return listOfAllLogisticProviders.stream().map(logisticProvider ->
                objectMapper.convertValue(logisticProvider, LogisticProviderOutGoingDto.class)
        ).toList();
    }

    /**
     * Get Logistic provider by code and is pull shipments true
     */
    protected Set<LogisticProvider> getLogisticProvidersWithEnabledPullShipments(LogisticProviderCodes logisticProviderCode) {
        Set<LogisticProvider> logisticProviders = logisticProviderRepository.findByLogisticProviderCodeAndIsPullShipmentsEnabledTrue(logisticProviderCode.getCode());
        if (CollectionUtils.isEmpty(logisticProviders)) {
            throw new EntityNotFoundException(String.format("Error: Logistic providers are not set in DB for %s, code %s", logisticProviderCode.getLogisticProviderName(), logisticProviderCode.getCode()));
        }
        return logisticProviders;
    }

    protected LogisticProvider getLogisticProvidersById(long id) {
        var logisticProvider = logisticProviderRepository.findById(id);
        if (logisticProvider.isPresent()) {
            return logisticProvider.get();
        }
        throw new EntityNotFoundException(String.format("Error: Logistic provider with id %s is not exists", id));
    }

    public ShipmentOutgoingDto getShipmentDto(String referenceId) {
        ShipmentRequest shipmentRequest = getShipment(referenceId);
        return mapTpShipmentOutgoingDto(shipmentRequest);
    }

    public ShipmentRequest getShipment(String referenceId) {
        ShipmentRequest shipmentRequest = shipmentRequestRepository.findByReferenceId(referenceId);
        if (shipmentRequest == null) {
            throw new EntityNotFoundException(String.format("Error: Shipment with referent id %s is not exists", referenceId));
        }
        return shipmentRequest;
    }

    public ShipmentRequest getShipmentByAwbNo(String awbNo) {
        ShipmentRequest shipmentRequest = shipmentRequestRepository.findByAwb(awbNo);
        if (shipmentRequest == null) {
            throw new EntityNotFoundException(String.format("Error: Shipment with awb no %s is not exists", awbNo));
        }
        return shipmentRequest;
    }


    private ShipmentOutgoingDto mapTpShipmentOutgoingDto(ShipmentRequest shipmentRequest) {
        return objectMapper.convertValue(shipmentRequest, ShipmentOutgoingDto.class);
    }

    /**
     * Create shipment request
     * Send updates to logistic provider updates topic about the logistic provider id, and order new status
     */
    protected ShipmentOutgoingDto saveShipmentRequest(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest, String externalStatus, String status, Long externalTaskId, boolean isSentSuccessfullyToThirdParty) throws Exception {
        ShipmentRequest shipmentRequest = mapToShipmentRequest(shipmentCreateIncomingRequest, externalStatus, status, externalTaskId, isSentSuccessfullyToThirdParty);
        long orderId = Long.parseLong(shipmentCreateIncomingRequest.getShipmentId());
        // Save shipment
        shipmentRequest = shipmentRequestRepository.save(shipmentRequest);
        // Build outgoing updates DTO
        OrderDto updatedOrderDto = OrderDto.builder().id(orderId).logisticProviderId(shipmentCreateIncomingRequest.getLogisticProviderId().longValue()).build();
        OrderUpdateOutgoingDto orderUpdateOutgoingDto = OrderUpdateOutgoingDto.builder().order(updatedOrderDto).comment(buildLogisticProviderComment(shipmentCreateIncomingRequest.getLogisticProviderId())).build();
        // Send order update event to sync changes with other BE services
        kafkaProducerService.sendDeliveryUpdateEvent(orderUpdateOutgoingDto,
                KafkaEventSource.THIRD_PARTY_LOGISTIC_CREATE);
        return mapTpShipmentOutgoingDto(shipmentRequest);
    }

    /**
     * Create shipment request (status, external status, external task id are null)
     * Send updates to logistic provider updates topic about the logistic provider id, and order new status
     */
    protected ShipmentOutgoingDto saveShipmentRequest(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest, boolean isSentSuccessfullyToThirdParty) throws Exception {
        return saveShipmentRequest(shipmentCreateIncomingRequest, null, null, null, isSentSuccessfullyToThirdParty);
    }


    /**
     * Update shipment request
     * Send updates to logistic provider updates topic about the logistic provider id, and order new status
     */
    protected ShipmentOutgoingDto updateShipmentRequest(String referenceId, String externalStatus, String mappedStatus, String description) throws Exception {

        ShipmentRequest shipmentRequest = getShipment(referenceId);
        // Set Internal status
        shipmentRequest.setStatus(mappedStatus);
        // Set External status
        shipmentRequest.setExternalStatus(externalStatus);
        // Update shipment request with the new statuses
        shipmentRequest = shipmentRequestRepository.save(shipmentRequest);
        // Build order update dto and send event to sync order updates
        OrderDto updatedOrderDto = OrderDto.builder().id(Long.parseLong(referenceId)).status(mappedStatus).build();
        String comment = buildStatusComment(shipmentRequest.getLogisticProviderId(), externalStatus, description);
        OrderUpdateOutgoingDto orderUpdateOutgoingDto = OrderUpdateOutgoingDto.builder().order(updatedOrderDto).comment(comment).build();
        kafkaProducerService.sendDeliveryUpdateEvent(orderUpdateOutgoingDto, KafkaEventSource.THIRD_PARTY_LOGISTIC_UPDATE);
        return mapTpShipmentOutgoingDto(shipmentRequest);
    }

    /**
     * Create comment text about status changed
     */
    private String buildStatusComment(long logisticProviderId, String externalStatus, String description) {
        LogisticProvider logisticProvider = getLogisticProvidersById(logisticProviderId);
        String comment = String.format("Updates from logistic provider (%s), external status changed to: %s", logisticProvider.getName(), externalStatus);
        if (StringUtils.isNotEmpty(description)) {
            return comment.concat(String.format(", description: %s", description));
        }
        return comment;
    }

    /**
     * Create comment text about assigning the order to an logistic provider
     */
    private String buildLogisticProviderComment(long logisticProviderId) {
        LogisticProvider logisticProvider = getLogisticProvidersById(logisticProviderId);
        return String.format("order is assigned to logistic provider %s", logisticProvider.getName());
    }

    protected void validatePullShipmentsToken(String token) {
        if (!integrationStatusReaderAccessToken.equals(token)) {
            throw new UnauthorizedException("Invalid access token used to pull shipments");
        }
    }

    protected void validateUpdateShipmentToken(String accessToken, String response) {
        if (!updateShipmentAccessToken.equals(accessToken)) {
            throw new UnauthorizedException(String.format(
                    "INTEGRATION SERVICE ERROR! Unauthorized request to update order, %s",
                    response));
        }
    }

    /**
     * Send pull shipments update summary to slack
     */
    protected void sendPullShipmentSummaryToSlack(LogisticProviderCodes logisticProviderCode, List<String> updatedOrders) {
        this.slackService.postErrorMessage(String.format("""
                        %s Status Update Cron Job
                        Number of orders updated: %s
                        Updated Orders Ids: %s""",
                logisticProviderCode.getLogisticProviderName(), updatedOrders.size(), updatedOrders));
    }

}
