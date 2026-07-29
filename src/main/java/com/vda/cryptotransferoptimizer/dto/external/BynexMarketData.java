package com.vda.cryptotransferoptimizer.dto.external;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class BynexMarketData {

    private String code;
    private BigDecimal price;

}
