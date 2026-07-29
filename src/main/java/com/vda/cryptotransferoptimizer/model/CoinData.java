package com.vda.cryptotransferoptimizer.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CoinData {

    private String coinName;
    private String fiatName;
    private BigDecimal price;

}
