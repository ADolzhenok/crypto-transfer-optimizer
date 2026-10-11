package com.vda.cryptotransferoptimizer.service;

import com.vda.cryptotransferoptimizer.client.bynex.BynexClient;
import com.vda.cryptotransferoptimizer.config.BynexFeeProperties;
import com.vda.cryptotransferoptimizer.dto.request.CalculateRequest;
import com.vda.cryptotransferoptimizer.dto.response.TradeResult;
import com.vda.cryptotransferoptimizer.dto.response.TransferResponse;
import com.vda.cryptotransferoptimizer.exceptions.PairNotFoundException;
import com.vda.cryptotransferoptimizer.model.CoinData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferCalculationServiceTest {

    @Mock
    BynexClient bynexClient;
    @Mock
    BynexFeeProperties bynexFeeProperties;
    @Mock
    TradeCalculator tradeCalculator;
    @Mock
    WithdrawalCalculator withdrawalCalculator;

    @InjectMocks
    TransferCalculationService transferCalculationService;

    @Test
    void shouldCalculateFiatToCoin(){
        //arrange
        String fiatName = "USD";
        String coinName = "USDT";
        BigDecimal amount = new BigDecimal("100");
        BigDecimal price = new BigDecimal("2");

        CalculateRequest request = CalculateRequest.builder()
                .fiatName(fiatName)
                .coinName(coinName)
                .amount(amount)
                .build();

        CoinData pairInfo = CoinData.builder()
                .fiatName(fiatName)
                .coinName(coinName)
                .price(price)
                .build();

        BigDecimal orderFee = BigDecimal.ZERO;

        TradeResult tradeResult = TradeResult.builder()
                .fiatNetAmount(amount)
                .coinAmount(new BigDecimal("50"))
                .tradingFee(orderFee + " " + fiatName)
                .build();

        BynexFeeProperties.FeeGroup individuals = new BynexFeeProperties.FeeGroup();
        individuals.setTaker(BigDecimal.ZERO);

        BynexFeeProperties.TradingFee tradingFee = new BynexFeeProperties.TradingFee();
        tradingFee.setIndividuals(individuals);

        //arrange - mock dependencies
        when(bynexClient.getPairData(request))
                .thenReturn(pairInfo);
        when(bynexFeeProperties.getTradingFee())
                .thenReturn(tradingFee);

        when(tradeCalculator.calculate(fiatName, price, amount, orderFee))
                .thenReturn(tradeResult);
        when(withdrawalCalculator.calculateWithdrawalResult(
                tradeResult.coinAmount(), coinName))
                .thenReturn(List.of());


        //act
        TransferResponse result = transferCalculationService.calculate(
                request
        );

        //assert
        assertEquals("USDT-USD", result.getCurrencyPair());
        assertEquals("TAKER", result.getOrderType());
        assertEquals(0, orderFee.compareTo(result.getOrderTypeFee()));
        assertEquals(0, amount.compareTo(result.getFiatAmount()));
        assertEquals(0, price.compareTo(result.getPrice()));

    }

    @Test
    void shouldThrowPairNotFoundExceptionWhenPairDoesntExist(){
        //arrange
        String fiatName = "USD";
        String coinName = "USDT";
        BigDecimal amount = new BigDecimal("100");

        CalculateRequest request = CalculateRequest.builder()
                .fiatName(fiatName)
                .coinName(coinName)
                .amount(amount)
                .build();

        when(bynexClient.getPairData(request))
                .thenReturn(null);

        assertThrows(
                PairNotFoundException.class,
                () -> transferCalculationService.calculate(request)
        );
    }

}