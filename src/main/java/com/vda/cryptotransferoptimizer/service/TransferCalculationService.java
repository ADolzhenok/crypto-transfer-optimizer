package com.vda.cryptotransferoptimizer.service;

//import com.vda.cryptotransferoptimizer.client.bybit.BybitClient;
import com.vda.cryptotransferoptimizer.client.bybit.BybitClient;
import com.vda.cryptotransferoptimizer.client.bybit.BybitResponseParser;
import com.vda.cryptotransferoptimizer.client.bynex.BynexClient;
import com.vda.cryptotransferoptimizer.client.bynex.BynexFeeResponseParser;
import com.vda.cryptotransferoptimizer.config.BynexFeeProperties;
import com.vda.cryptotransferoptimizer.dto.request.CalculateRequest;
import com.vda.cryptotransferoptimizer.dto.response.WithdrawalResult;
import com.vda.cryptotransferoptimizer.dto.response.TransferResponse;
import com.vda.cryptotransferoptimizer.exceptions.CoinNotFoundException;
import com.vda.cryptotransferoptimizer.exceptions.PairNotFoundException;
import com.vda.cryptotransferoptimizer.model.CoinData;
import com.vda.cryptotransferoptimizer.model.WithdrawalNetworksInfo;
import com.vda.cryptotransferoptimizer.dto.response.TradeResult;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TransferCalculationService {

    private final BynexClient bynexClient;
    private final BybitClient bybitClient;
    private final BynexFeeProperties bynexFeeProperties;
    private final BynexFeeResponseParser bynexFeeResponseParser;
    private final BybitResponseParser bybitResponseParser;

    public TransferResponse calculate(CalculateRequest request){
        BigDecimal fiatAmount = request.getAmount();

        CoinData bynexPairInfo = bynexClient.getPairData(request);
        if (bynexPairInfo == null){
            throw new PairNotFoundException(
                    String.format(
                            "%s - %s cannot be found in Bynex API",
                            request.getCoinName(),
                            request.getFiatName()
                    )
            );
        }
        BigDecimal orderFee = findOrderFee();

//        bynexPairInfo.setPrice(new BigDecimal("3.01494"));
//        orderFee = new BigDecimal("0.25");

        TradeResult tradeResult =
                calculateBynexTradeResult(bynexPairInfo, fiatAmount, orderFee);

        List<WithdrawalResult> withdrawalResultList =
                calculateWithdrawalResult(tradeResult.coinAmount(), bynexPairInfo.getCoinName());

        return TransferResponse.builder()
                .currencyPair(bynexPairInfo.getCoinName()+"-"+bynexPairInfo.getFiatName())
                .price(bynexPairInfo.getPrice())
                .orderType("TAKER")
                .orderTypeFee(orderFee)
                .fiatAmount(fiatAmount)
                .tradeResult(tradeResult)
                .withdrawalResult(withdrawalResultList)
                .build();
    }

    //TODO Realization MAKER calculating
    // calling findOrderFee
    private BigDecimal findOrderFee(){
        return bynexFeeProperties
                .getTradingFee()
                .getIndividuals()
                .getTaker();
    }

    //TODO Realization MAKER calculating
//    private BigDecimal findOrderFee(OrderType orderType){
//        if (orderType == OrderType.MAKER){
//            return bynexFeeProperties
//                    .getTradingFee()
//                    .getIndividuals()
//                    .getMaker();
//        }
//        else {
//            return bynexFeeProperties
//                    .getTradingFee()
//                    .getIndividuals()
//                    .getTaker();
//        }
//    }


    private TradeResult calculateBynexTradeResult(
            CoinData pairInfo,
            BigDecimal fiatAmount,
            BigDecimal orderFee
    ){
        BigDecimal orderCommission = fiatAmount
                .multiply(
                        orderFee.divide(new BigDecimal("100"), 5, RoundingMode.HALF_UP)
                )
                .setScale(5, RoundingMode.HALF_UP);
        BigDecimal fiatNetAmount = fiatAmount.subtract(orderCommission)
                .setScale(5, RoundingMode.HALF_UP);

        BigDecimal coinAmount = fiatNetAmount.divide(pairInfo.getPrice(), 5, RoundingMode.HALF_UP);

        return TradeResult.builder()
                .tradingFee(orderCommission + " " + pairInfo.getFiatName())
                .fiatNetAmount(fiatNetAmount)
                .coinAmount(coinAmount)
                .build();
    }

    private List<WithdrawalResult> calculateWithdrawalResult(BigDecimal coinAmount, String coinName){
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
            BigDecimal coinNetAmount = coinAmount.subtract(withdrawalNetworksInfo.getFee());
            BigDecimal usdtAmount = coinName.equals("USDT") ?
                    coinNetAmount :
                    parseToUsdt(coinName, coinNetAmount);
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

    private BigDecimal parseToUsdt(String coinName, BigDecimal coinNetAmount) {
        String bybitPairName = coinName + "USDT";
        JsonNode bybitResponse = bybitClient.getPairData(bybitPairName);
        CoinData bybitPairInfo = bybitResponseParser.parse(bybitPairName, bybitResponse);

        return bybitPairInfo.getPrice().multiply(coinNetAmount);
    }

}
