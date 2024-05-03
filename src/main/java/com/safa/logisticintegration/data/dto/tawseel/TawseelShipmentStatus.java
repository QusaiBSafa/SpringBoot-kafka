package com.safa.logisticintegration.data.dto.tawseel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TawseelShipmentStatus {
    PENDING_PICKUP("PENDING PICKUP"),
    PICKUP_DONE("PICKUP DONE"),
    PICKUP_FAILED("PICKUP FAILED"),
    IN_TRANSIT("IN-TRANSIT"),
    OUT_FOR_DELIVERY("OUT FOR DELIVERY"),
    DELIVERED("DELIVERED"),
    UNDELIVERED("UNDELIVERED"),
    RTO_INITIATED("RTO INITIATED"),
    RTO_IN_TRANSIT("RTO IN-TRANSIT"),
    RTO_OUT_FOR_DELIVERY("RTO OUT FOR DELIVERY"),
    RTO_DELIVERED("RTO DELIVERED"),
    PICKUP_CANCEL_BY_CLIENT("PICKUP CANCEL BY CLIENT"),
    CANCELLED_RETURNED("CANCELLED RETURNED");


    private final String status;

    public static TawseelShipmentStatus getByStatus(String status) {
        for (TawseelShipmentStatus shipmentStatus : TawseelShipmentStatus.values()) {
            if (shipmentStatus.getStatus().equalsIgnoreCase(status)) {
                return shipmentStatus;
            }
        }
        return null;
    }

}
