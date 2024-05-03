package com.safa.logisticintegration.data.dto.transcorp;

import lombok.Getter;

@Getter
public enum TranscorpTriggerType {
    TASK_OUT_FOR_DELIVERY,
    TASK_FAILED,
    TASK_CANCELED,
    TASK_COMPLETED,
    OUT_FOR_DELIVERY,
    FAILED,
    CANCELED,
    DELIVERED,
    IN_TRANSIT,
    ORDERED,
    PICKED_UP,
    ARRIVED_IN_DC,
    PROCESS_FOR_RETURN,
    RETURNED_TO_SHIPPER,
    HUB_TRANSFER,
    RESCHEDULED,
    REATTEMPT,
    RETURNED_TO_DC,
    UNASSIGNED,
    ASSIGNED,
    DUPLICATE,
    NOT_RECEIVED,
    COMPLETED,

}
