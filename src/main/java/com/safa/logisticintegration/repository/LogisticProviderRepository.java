package com.safa.logisticintegration.repository;

import com.safa.logisticintegration.data.entity.LogisticProvider;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface LogisticProviderRepository extends CrudRepository<LogisticProvider, Long> {

    Set<LogisticProvider> findByLogisticProviderCodeAndIsPullShipmentsEnabledTrue(String logisticProviderCode);


}
