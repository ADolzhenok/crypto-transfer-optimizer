package com.vda.cryptotransferoptimizer.service;

import com.vda.cryptotransferoptimizer.client.bybit.BybitClient;
import com.vda.cryptotransferoptimizer.client.bybit.BybitResponseParser;
import com.vda.cryptotransferoptimizer.client.bynex.BynexClient;
import com.vda.cryptotransferoptimizer.client.bynex.BynexFeeResponseParser;
import com.vda.cryptotransferoptimizer.dto.response.WithdrawalResult;
import com.vda.cryptotransferoptimizer.exceptions.CoinNotFoundException;
import com.vda.cryptotransferoptimizer.model.CoinData;
import com.vda.cryptotransferoptimizer.model.WithdrawalNetworksInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawalCalculatorTest {

    @Mock
    private BynexFeeResponseParser bynexFeeResponseParser;
    @Mock
    private BybitResponseParser bybitResponseParser;
    @Mock
    private BynexClient bynexClient;
    @Mock
    private BybitClient bybitClient;

    @InjectMocks
    private WithdrawalCalculator withdrawalCalculator;

    @Test
    void shouldCalculateCoinNetAmountAndConvertItToUsdt(){
        //arrange - input & expected data
        String coinName = "BNB";
        String feeHtml = "<html>test</html>";
        String jsonBuildId = "build-id";
        String pairUsdt = "BNBUSDT";

        BigDecimal coinAmount = new BigDecimal("100");

        WithdrawalNetworksInfo withdrawalNetworksInfo = WithdrawalNetworksInfo.builder()
                .coinName("BNB")
                .blockchainNetwork("BSC")
                .fee(new BigDecimal("2"))
                .build();

        JsonNode feesJson = JsonNodeFactory.instance.objectNode();
        JsonNode bybitResponse = JsonNodeFactory.instance.objectNode();

        CoinData bybitPairInfo = CoinData.builder()
                .price(new BigDecimal("100"))
                .build();

        //arrange - mock Bybit dependencies
        when(bynexClient.getHtmlForFeesJson())
                .thenReturn(feeHtml);

        when(bynexFeeResponseParser.parseHtml(feeHtml))
                .thenReturn(jsonBuildId);

        when(bynexClient.getFeesJson(jsonBuildId))
                .thenReturn(feesJson);

        when(bynexFeeResponseParser.parsePage(coinName, feesJson))
                .thenReturn(List.of(withdrawalNetworksInfo));

        //arrange - mock Bybit dependencies
        when(bybitClient.getPairData(pairUsdt))
                .thenReturn(bybitResponse);

        when(bybitResponseParser.parse(pairUsdt, bybitResponse))
                .thenReturn(bybitPairInfo);

        //act
        List<WithdrawalResult> withdrawalResult =
                withdrawalCalculator.calculateWithdrawalResult(coinAmount, coinName);

        //assert
        assertEquals(1, withdrawalResult.size());
        assertEquals("BSC", withdrawalResult.getFirst().getNetwork());
        assertEquals("2 BNB", withdrawalResult.getFirst().getNetworkWithdrawalLoss());
        assertEquals(0, new BigDecimal("98").compareTo(withdrawalResult.getFirst().getCoinNetAmount()));
        assertEquals(0, new BigDecimal("9800").compareTo(withdrawalResult.getFirst().getCoinNetAmountInUsdt()));
    }

    @Test
    void shouldConvertCoinAMountToUsdt(){
        //arrange - input & expected data
        String coinName = "BNB";
        String pairName = "BNBUSDT";
        BigDecimal coinNetAmount = new BigDecimal("98");

        JsonNode bybitResponse = JsonNodeFactory.instance.objectNode();

        CoinData bybitPairInfo = CoinData.builder()
                .price(new BigDecimal("100"))
                .build();

        //arrange - mock dependencies
        when(bybitClient.getPairData(pairName))
                .thenReturn(bybitResponse);

        when(bybitResponseParser.parse(pairName,bybitResponse))
                .thenReturn(bybitPairInfo);

        //act
        BigDecimal coinInUsdt = withdrawalCalculator.parseToUsdt(coinName, coinNetAmount);

        //assert
        assertEquals(0, new BigDecimal("9800").compareTo(coinInUsdt));
    }

    @Test
    void shouldThrowCoinNotFoundExceptionWhenCoinIsMissingFromWithdrawalFeeList(){
        //arrange
        String coinName = "BNB";
        String feeHtml = "<html>test</html>";
        String jsonBuildId = "build-id";

        BigDecimal coinAmount = new BigDecimal("100");
        JsonNode feesJson = JsonNodeFactory.instance.objectNode();

        when(bynexClient.getHtmlForFeesJson())
                .thenReturn(feeHtml);

        when(bynexFeeResponseParser.parseHtml(feeHtml))
                .thenReturn(jsonBuildId);

        when(bynexClient.getFeesJson(jsonBuildId))
                .thenReturn(feesJson);

        when(bynexFeeResponseParser.parsePage(coinName, feesJson))
                .thenReturn(List.of());

        //assert
        CoinNotFoundException ex = assertThrows(CoinNotFoundException.class, () ->
            withdrawalCalculator.calculateWithdrawalResult(coinAmount, coinName)
        );

        assertEquals(
                "Cannot be extract Bynex fees for coin BNB. Reason: coin is not found in the fee list",
                ex.getMessage()
        );


    }

}
