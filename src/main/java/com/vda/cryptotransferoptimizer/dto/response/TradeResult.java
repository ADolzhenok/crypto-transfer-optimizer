package com.vda.cryptotransferoptimizer.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TradeResult(
        String tradingFee,
        BigDecimal fiatNetAmount,
        BigDecimal coinAmount
) {
}
