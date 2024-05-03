package com.safa.logisticintegration.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shipment_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRequest extends BaseEntity {

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "referenceId")
    private String referenceId;

    @Column(name = "externalTaskId")
    private long externalTaskId;

    @Column(name = "outOfDeliveryAt")
    private Date outOfDeliveryAt;

    @Column(name = "failedDeliveryAttemptReason")
    private String failedDeliveryAttemptReason;

    @Column(name = "totalShipmentQuantity")
    private String totalShipmentQuantity;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "deliveryStartTime")
    private Date deliveryStartTime;

    @Column(name = "deliveryEndTime")
    private Date deliveryEndTime;

    @Column(name = "status")
    private String status;

    @Column(name = "externalStatus")
    private String externalStatus;

    @Column(name = "logisticProviderId")
    private Integer logisticProviderId;

    @Column(name = "awb")
    private String awb;

    @Column(name = "logisticProviderCode")
    private String logisticProviderCode;

    @Column(name = "street")
    private String street;
    @Column(name = "apartment")
    private String apartment;
    @Column(name = "building")
    private String building;

    @Column(name = "city")
    private String city;

    @Column(name = "area")
    private String area;
    /**
     * If the shipment is sent successfully to third party then set this to true
     */
    @Column(name = "isSentSuccessfullyToThirdParty", columnDefinition = "boolean default false")
    private boolean isSentSuccessfullyToThirdParty;
}
