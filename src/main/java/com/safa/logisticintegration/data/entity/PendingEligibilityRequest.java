package com.safa.logisticintegration.data.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Setter
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "pending_eligibility")
public class PendingEligibilityRequest extends BaseEntity {

    /**
     * Consultation ID, order ID
     */
    @Column(name = "referenceId")
    private long referenceId;

    /**
     * consultation, order, lab
     */
    @Column(name = "referenceType")
    private String referenceType;


    @Column(name = "identityNumber")
    private String identityNumber;

    @Column(name = "externalEligibilityId")
    private long externalEligibilityId;

}
