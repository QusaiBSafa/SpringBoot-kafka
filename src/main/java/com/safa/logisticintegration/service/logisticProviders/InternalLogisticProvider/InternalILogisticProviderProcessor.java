package com.safa.logisticintegration.service.logisticProviders.InternalLogisticProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safa.logisticintegration.common.LogisticProviderCodes;
import com.safa.logisticintegration.data.dto.order.ShipmentCreateIncomingRequest;
import com.safa.logisticintegration.data.dto.shipment.ShipmentOutgoingDto;
import com.safa.logisticintegration.data.entity.ShipmentRequest;
import com.safa.logisticintegration.repository.LogisticProviderRepository;
import com.safa.logisticintegration.repository.ShipmentRequestRepository;
import com.safa.logisticintegration.service.kafka.KafkaProducerService;
import com.safa.logisticintegration.service.logisticProviders.ILogisticProviderProcessor;
import com.safa.logisticintegration.service.logisticProviders.commonProcessors.CommonLogisticProviderProcessor;
import com.safa.logisticintegration.service.slack.SlackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class InternalILogisticProviderProcessor extends CommonLogisticProviderProcessor implements ILogisticProviderProcessor {


    public InternalILogisticProviderProcessor(ObjectMapper objectMapper, SlackService slackService, ShipmentRequestRepository shipmentRequestRepository, LogisticProviderRepository logisticProviderRepository, KafkaProducerService kafkaProducerService) {
        super(objectMapper, slackService, shipmentRequestRepository, logisticProviderRepository, kafkaProducerService);
    }

    @Override
    public ShipmentOutgoingDto createShipment(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest) throws Exception {
        // For internal logistic provider no need to create any shipment we just set the logistic provider id into the order by sending updates to the related topic
        return saveShipmentRequest(shipmentCreateIncomingRequest, false);
    }

    @Override
    public void updateShipment(String accessToken, Object incomingRequest) {
        // for internal logistic provider we are doing the updates throw driver app direct to the order in BE pharmacy service
        // So this is currently not needed
    }

    @Override
    public void pullShipments(String token) {
        // for internal logistic provider pull shipments is not supported
    }

    @Override
    public byte[] getShipmentLabel(ShipmentRequest shipmentRequest) {
        return new byte[0];
    }

    @Override
    public String getCode() {
        return LogisticProviderCodes.AHL.getCode();
    }


}
