package com.safa.logisticintegration.service.logisticProviders;

import com.safa.logisticintegration.data.dto.order.ShipmentCreateIncomingRequest;
import com.safa.logisticintegration.data.entity.LogisticProvider;


public interface ILogisticProviderTransformer<T> {

    String mapStatus(String thirdPartyStatus);

    T transformToThirdPartyRequest(ShipmentCreateIncomingRequest shipmentCreateIncomingRequest,
                                   LogisticProvider provider);

}
