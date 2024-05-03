package com.safa.logisticintegration.service.logisticProviders;

import com.safa.logisticintegration.data.dto.order.ShipmentCreateIncomingRequest;
import com.safa.logisticintegration.data.dto.shipment.ShipmentOutgoingDto;
import com.safa.logisticintegration.data.entity.ShipmentRequest;


public interface ILogisticProviderProcessor<T> {
    ShipmentOutgoingDto createShipment(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest) throws Exception;

    void updateShipment(String accessToken, T incomingResponse) throws Exception;

    void pullShipments(String token);

    byte[] getShipmentLabel(ShipmentRequest shipmentRequest);

    String getCode();

}
