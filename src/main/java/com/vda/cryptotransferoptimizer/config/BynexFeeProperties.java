package com.vda.cryptotransferoptimizer.config;

import lombok.Data;
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

    @Data
    public static class TradingFee {
        private FeeGroup individuals;
        private FeeGroup legalEntities;
    }

    @Data
    public static class FeeGroup {
        private BigDecimal taker;
        private BigDecimal maker;
        private BigDecimal pairsWithICOTokens;
    }

}
