package com.safa.logisticintegration.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.safa.logisticintegration.data.dto.elgibility.IncomingEligibilityCheck;
import com.safa.logisticintegration.data.dto.kafka.KafkaEvent;
import com.safa.logisticintegration.data.dto.order.ShipmentCreateIncomingRequest;
import com.safa.logisticintegration.exception.ExceptionMessages;
import com.safa.logisticintegration.exception.IntegrationException;
import com.safa.logisticintegration.service.eligibility.EligibilityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class KafkaConsumerService {

    public static final String SHIPMENT_CREATION_REQUEST_TOPIC = "logistic_shipment_update";
    public static final String SHIPMENT_CREATION_REQUEST_GROUP_ID = "logistic_create_shipment_group_id";

    private final EligibilityService eligibilityService;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public KafkaConsumerService(EligibilityService eligibilityService, ObjectMapper mapper) {
        this.eligibilityService = eligibilityService;
    }


    @KafkaListener(topics = "${topic.consumer.eligibility-request.name}", groupId = "eligibility_check_request-group1")
    private void eligibilityCheckConsumer(ConsumerRecord<String, String> record) throws Exception {
        String event = record.value();
        KafkaEvent<IncomingEligibilityCheck> kafkaEvent =
                null;
        try {
            kafkaEvent = mapper.readValue(
                    event, new TypeReference<KafkaEvent<IncomingEligibilityCheck>>() {
                    });
        } catch (JsonProcessingException e) {
            throw new IntegrationException(String.format(ExceptionMessages.KAFKA_PROCESSING_ERROR, event), e);
        }
        eligibilityService.createOpenJetEligibilityCheckRequest(kafkaEvent.getDetails());
    }

    /**
     * Map to kafka transaction event
     */
    public static KafkaEvent<ShipmentCreateIncomingRequest> mapTo(String event)
            throws JsonProcessingException {
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(
                event, new TypeReference<>() {
                });
    }

}
