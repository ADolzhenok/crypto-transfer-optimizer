package com.vda.cryptotransferoptimizer.service;

import com.vda.cryptotransferoptimizer.dto.response.TradeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TradeCalculatorTest {

    private TradeCalculator tradeCalculator;
    private String fiatName;
    private BigDecimal price;
    private BigDecimal fiatAmount;


    @BeforeEach
    void setUp(){
        tradeCalculator = new TradeCalculator();
        fiatName = "USD";
        price = new BigDecimal("2");
        fiatAmount = new BigDecimal("100");
    }

    @Test
    void shouldReturnFiatAmountWhenFeeIsZero(){
        //arrange
        BigDecimal orderFee = BigDecimal.ZERO;

        //act
        TradeResult result = tradeCalculator.calculate(fiatName, price, fiatAmount, orderFee);

        //assert
        assertEquals(0, fiatAmount.compareTo(result.fiatNetAmount()));
    }

    @Test
    void shouldReturnHalfOfAmountWith50PercentFeeInFiatNetAmount() {
        BigDecimal orderFee = new BigDecimal("50");
        BigDecimal expectedAmount = new BigDecimal("50");

        TradeResult result = tradeCalculator.calculate(fiatName, price, fiatAmount, orderFee);

        assertEquals(0, expectedAmount.compareTo(result.fiatNetAmount()));
    }

    @Test
    void shouldCalculateCoinAmountUsingPrice(){
        BigDecimal orderFee = BigDecimal.ZERO;
        BigDecimal expectedCoinAmount = new BigDecimal("50");

        TradeResult result = tradeCalculator.calculate(fiatName, price, fiatAmount, orderFee);

        assertEquals(0, expectedCoinAmount.compareTo(result.coinAmount()));
    }

    @Test
    void shouldReturnStringIncludingTradingFeeAndFiatName() {
        BigDecimal orderFee = BigDecimal.TEN;

        TradeResult result = tradeCalculator.calculate(fiatName, price, fiatAmount, orderFee);

        assertEquals("10.00000 USD", result.tradingFee());


    }


}
