package com.safa.logisticintegration.service.logisticProviders;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@AllArgsConstructor
public class LogisticProviderFactory {

    protected static Map<String, ILogisticProviderProcessor> processorsCache = new HashMap<>();

    public List<ILogisticProviderProcessor> processors;

    @PostConstruct
    void initCache() {
        processors.forEach(processor -> processorsCache.put(
                processor.getCode(), processor));
    }

    public ILogisticProviderProcessor getInstance(String code) {
        if (processorsCache.containsKey(code)) {
            return processorsCache.get(code);
        } else {
            throw new UnsupportedOperationException(String.format("No implementation found for processor code %s", code));
        }
    }
}
