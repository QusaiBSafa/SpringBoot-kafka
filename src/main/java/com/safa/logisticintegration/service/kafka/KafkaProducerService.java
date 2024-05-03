package com.safa.logisticintegration.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safa.logisticintegration.data.dto.elgibility.OutgoingEligibilityCheck;
import com.safa.logisticintegration.data.dto.kafka.KafkaEvent;
import com.safa.logisticintegration.data.dto.kafka.KafkaEventName;
import com.safa.logisticintegration.data.dto.kafka.KafkaEventSource;
import com.safa.logisticintegration.data.dto.kafka.KafkaEventType;
import com.safa.logisticintegration.data.dto.order.OrderUpdateOutgoingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * This service send kafka events to a specified topic
 *
 * @author Qusai Safa
 */
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;
    Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Value("${topic.producer.logistic-shipment-update.name:logistic_shipment_update}")
    private String logisticUpdateTopic;

    @Value("${topic.producer.eligibility-status-update.name}")
    private String eligibilityUpdateTopic;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    public void sendDeliveryUpdateEvent(OrderUpdateOutgoingDto outGoingDtoOrder, KafkaEventSource kafkaEventSource) throws Exception {
        String kafkaEvent = this.buildKafkaEvent(outGoingDtoOrder, KafkaEventType.UPDATE, kafkaEventSource, KafkaEventName.ORDER);
        send(logisticUpdateTopic, kafkaEvent, 3);
    }

    @Async
    public void sendEligibilityCheckUpdates(OutgoingEligibilityCheck outgoingEligibilityCheck, KafkaEventSource kafkaEventSource) throws Exception {
        String kafkaEvent = this.buildKafkaEvent(outgoingEligibilityCheck, KafkaEventType.UPDATE, kafkaEventSource, KafkaEventName.CONSULTATION);
        send(eligibilityUpdateTopic, kafkaEvent, 3);
    }

    private <T> String buildKafkaEvent(T data, KafkaEventType kafkaEventType, KafkaEventSource kafkaEventSource, KafkaEventName kafkaEventName) throws Exception {
        KafkaEvent<T> kafkaEvent =
                new KafkaEvent<T>();
        kafkaEvent.setEventName(kafkaEventName.getName());
        kafkaEvent.setEventType(kafkaEventType.getLabel());
        kafkaEvent.setEventSource(kafkaEventSource.getSource());
        kafkaEvent.setDetails(data);
        String kafkaMessage = null;
        try {
            kafkaMessage = this.objectMapper.writeValueAsString(kafkaEvent);
        } catch (JsonProcessingException e) {
            throw new Exception(e.getMessage(), e);
        }
        return kafkaMessage;
    }

    private void send(String transactionTopic, String message, Integer retryTimes) {
        try {
            kafkaTemplate.send(transactionTopic, message);
            logger.info("Send message to kafka topic({}): {}", transactionTopic, message);
        } catch (Exception e) {
            logger.error("Error while sending kafka event", e);
            if (retryTimes > 0) {
                logger.info("Retry sending the event to kafka, retry number:" + retryTimes);
                send(transactionTopic, message, --retryTimes);
            }
        }
    }


}
