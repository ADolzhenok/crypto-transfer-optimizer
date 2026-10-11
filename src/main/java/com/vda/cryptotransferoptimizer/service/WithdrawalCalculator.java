package com.vda.cryptotransferoptimizer.service;

import com.vda.cryptotransferoptimizer.client.bybit.BybitClient;
import com.vda.cryptotransferoptimizer.client.bybit.BybitResponseParser;
import com.vda.cryptotransferoptimizer.client.bynex.BynexClient;
import com.vda.cryptotransferoptimizer.client.bynex.BynexFeeResponseParser;
import com.vda.cryptotransferoptimizer.dto.response.WithdrawalResult;
import com.vda.cryptotransferoptimizer.exceptions.CoinNotFoundException;
import com.vda.cryptotransferoptimizer.model.CoinData;
import com.vda.cryptotransferoptimizer.model.WithdrawalNetworksInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class WithdrawalCalculator {

    private final BynexFeeResponseParser bynexFeeResponseParser;
    private final BybitResponseParser bybitResponseParser;
    private final BynexClient bynexClient;
    private final BybitClient bybitClient;

    public List<WithdrawalResult> calculateWithdrawalResult(BigDecimal coinAmount, String coinName) {
        String jsonBuildId = bynexFeeResponseParser.parseHtml(bynexClient.getHtmlForFeesJson());
        final List<WithdrawalNetworksInfo> networksList =
                bynexFeeResponseParser.parsePage(coinName, bynexClient.getFeesJson(jsonBuildId));
        if (networksList.isEmpty()){
            throw new CoinNotFoundException(
                    String.format(
                            "Cannot be extract Bynex fees for coin %s. Reason: coin is not found in the fee list",
                            coinName
                    )
            );
        }
        List<WithdrawalResult> withdrawalResultList = new ArrayList<>();

        for (WithdrawalNetworksInfo withdrawalNetworksInfo : networksList) {
            BigDecimal coinNetAmount = BigDecimal.ZERO;
            BigDecimal usdtAmount = BigDecimal.ZERO;
            if (coinAmount.compareTo(withdrawalNetworksInfo.getFee()) > 0){
                coinNetAmount = coinAmount.subtract(withdrawalNetworksInfo.getFee());
                usdtAmount = coinName.equals("USDT") ?
                        coinNetAmount :
                        parseToUsdt(coinName, coinNetAmount);
            }
            withdrawalResultList.add(
                    WithdrawalResult.builder()
                            .network(withdrawalNetworksInfo.getBlockchainNetwork())
                            .networkWithdrawalLoss(
                                    String.format(
                                            "%s %s", withdrawalNetworksInfo.getFee().toPlainString(), coinName
                                    )
                            )
                            .coinNetAmount(coinNetAmount)
                            .coinNetAmountInUsdt(usdtAmount)
                            .build()
            );
        }
        return withdrawalResultList;
    }

    public BigDecimal parseToUsdt(String coinName, BigDecimal coinNetAmount) {
        String bybitPairName = coinName + "USDT";
        JsonNode bybitResponse = bybitClient.getPairData(bybitPairName);
        CoinData bybitPairInfo = bybitResponseParser.parse(bybitPairName, bybitResponse);

        return bybitPairInfo.getPrice().multiply(coinNetAmount);
    }

}
