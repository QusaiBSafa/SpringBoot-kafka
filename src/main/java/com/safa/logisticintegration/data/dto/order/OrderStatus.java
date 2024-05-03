package com.safa.logisticintegration.data.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    NEW_ORDER("New order"),
    NEW_PRESCRIPTION("New prescription"),
    PHARMACY_REVIEW("Pharmacy Review"),
    AMEND_PRESCRIPTION("Amend Prescription"),
    INSURANCE_REVIEW("Insurance Review"),
    APPROVED_BY_INSURANCE("Approved by Insurance"),
    PARTIALLY_APPROVED_BY_INSURANCE("Partially approved by Insurance"),
    PARTIALLY_COVERAGE_CONFIRMED_BY_USER("Partially coverage confirmed by user"),
    CONFIRM_MEDICATION("Confirm medication"),
    MEDICATION_CONFIRMED("Medication Confirmed"),
    CONFIRM_DELIVERY_TIME("Confirm delivery time"),
    CONFIRM_DELIVERY_ADDRESS("Confirm delivery address"),
    DELIVERY_TIME_CONFIRMED("Delivery time Confirmed"),
    DELIVERY_ADDRESS_CONFIRMED("Delivery address confirmed"),
    PREPARING("Preparing / scheduled"),
    REVIEW_REQUESTED("Review Requested"),
    REJECTED_BY_INSURANCE("Rejected by Insurance"),
    SELF_PAYMENT_REQUESTED("Self payment requested"),
    PAYMENT_PROCESSED("Payment processed"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered"),
    DONE("Done"),
    PARTIALLY_DELIVERED("Partially Delivered"),
    FAILED_DELIVERY_ATTEMPT("Failed Delivery Attempt"),
    REFUSED("Refused"), //TBR
    REFUSED_BY_PHARMACY("Refused by pharmacy"),
    CANCELLED("Cancelled"),
    READY_FOR_DELIVERY("Ready for delivery");

    private final String status;
}
