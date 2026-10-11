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

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class TransferCalculationService {

    private final BynexClient bynexClient;
    private final BynexFeeProperties bynexFeeProperties;
    private final TradeCalculator tradeCalculator;
    private final WithdrawalCalculator withdrawalCalculator;

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

//       bynexPairInfo.setPrice(new BigDecimal("1.00205"));
//       orderFee = new BigDecimal("0.025");

        TradeResult tradeResult = tradeCalculator.calculate(
                bynexPairInfo.getFiatName(),
                bynexPairInfo.getPrice(),
                fiatAmount,
                orderFee
        );

        List<WithdrawalResult> withdrawalResultList =
                withdrawalCalculator.calculateWithdrawalResult(
                        tradeResult.coinAmount(),
                        bynexPairInfo.getCoinName()
                );

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

}
