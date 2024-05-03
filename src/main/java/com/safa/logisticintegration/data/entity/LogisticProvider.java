package com.safa.logisticintegration.data.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Setter
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "logistic_provider")
@NoArgsConstructor
public class LogisticProvider extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "accountId")
    private String accountId; // use as api_key for tawseel

    @Column(name = "username")
    private String username; // use as vendor name for tawseel

    @Column(name = "password")
    private String password; // use as secret key for tasweel

    @Column(name = "customerId")
    private Long customerId;

    @Column(name = "logisticProviderCode")
    private String logisticProviderCode;

    /**
     * Logistic provider branch address details
     */
    @Embedded
    private BranchAddress branchAddress;

    @Column(name = "isPullShipmentsEnabled", columnDefinition = "boolean default true")
    private boolean isPullShipmentsEnabled;

    @Column(name = "isCreateShipmentEnabled", columnDefinition = "boolean default true")
    private boolean isCreateShipmentEnabled;

}
