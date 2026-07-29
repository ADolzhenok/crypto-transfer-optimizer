package com.vda.cryptotransferoptimizer.dto.external;

import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class BybitMarketData {

    String symbol;
    BigDecimal lastPrice;

}
