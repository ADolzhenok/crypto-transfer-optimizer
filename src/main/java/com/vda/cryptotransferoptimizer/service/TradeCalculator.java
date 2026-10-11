package com.vda.cryptotransferoptimizer.service;

import com.vda.cryptotransferoptimizer.dto.response.TradeResult;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TradeCalculator {

    @NotEmpty
    public TradeResult calculate(
            String fiatName,
            BigDecimal price,
            BigDecimal fiatAmount,
            BigDecimal orderFee
    ) {
        BigDecimal orderCommission = fiatAmount
                .multiply(
                        orderFee.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)
                )
                .setScale(5, RoundingMode.HALF_UP);
        BigDecimal fiatNetAmount = fiatAmount.subtract(orderCommission)
                .setScale(5, RoundingMode.HALF_UP);

        BigDecimal coinAmount = fiatNetAmount.divide(price, 5, RoundingMode.HALF_UP);

        return TradeResult.builder()
                .tradingFee(orderCommission + " " + fiatName)
                .fiatNetAmount(fiatNetAmount)
                .coinAmount(coinAmount)
                .build();
    }

}
