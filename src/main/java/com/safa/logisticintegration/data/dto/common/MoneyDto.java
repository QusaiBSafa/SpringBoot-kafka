package com.safa.logisticintegration.data.dto.common;

import lombok.*;

import java.math.BigDecimal;

@Data
public class MoneyDto {

    private BigDecimal value;

    private String currency;

}
