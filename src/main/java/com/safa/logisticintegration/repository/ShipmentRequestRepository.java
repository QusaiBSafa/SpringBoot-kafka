package com.safa.logisticintegration.repository;

import com.safa.logisticintegration.data.entity.ShipmentRequest;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ShipmentRequestRepository extends CrudRepository<ShipmentRequest, Long> {

    ShipmentRequest findByReferenceId(String referenceId);

    ShipmentRequest findByExternalTaskId(Long externalTaskId);

    ShipmentRequest findByAwb(String awb);

    Set<ShipmentRequest> findAllByCreatedAtBetweenAndIsSentSuccessfullyToThirdPartyTrueAndStatusNotInAndLogisticProviderIdIn(LocalDateTime startDate, LocalDateTime endDate, List<String> statuses, List<Integer> logisticProviderIds);
}
