package com.vda.cryptotransferoptimizer.dto.external;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PairData {

    String pair;
    BigDecimal sell;
    BigDecimal buy;
    BigDecimal price;

}
