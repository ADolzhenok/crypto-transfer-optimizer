package com.vda.cryptotransferoptimizer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "exchange.bynex")
@Component
@Getter
@Setter
public class BynexFeeProperties {

    private TradingFee tradingFee;

    @Getter
    @Setter
    public static class TradingFee {
        private FeeGroup individuals;
        private FeeGroup legalEntities;
    }

    @Getter
    @Setter
    public static class FeeGroup {
        private BigDecimal taker;
        private BigDecimal maker;
        private BigDecimal pairsWithICOTokens;
    }

}
