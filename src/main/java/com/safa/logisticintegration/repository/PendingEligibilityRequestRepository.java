package com.safa.logisticintegration.repository;

import com.safa.logisticintegration.data.entity.PendingEligibilityRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PendingEligibilityRequestRepository extends CrudRepository<PendingEligibilityRequest, Long> {

    @Transactional
    Optional<PendingEligibilityRequest> deleteByExternalEligibilityId(long id);

    PendingEligibilityRequest findByReferenceIdAndReferenceTypeAndIdentityNumber(long referenceId, String referenceType, String identityNumber);
}
