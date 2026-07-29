package com.vda.cryptotransferoptimizer.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WithdrawalResult {

    //TODO Realization MAKER calculating
    // private OrderType orderType;
//    private String orderType;
    private BigDecimal coinNetAmount;
    private BigDecimal coinNetAmountInUsdt;
    private String network;
    private String networkWithdrawalLoss;
//    private String orderTypeFee;
//    private String tradeFee;

}
