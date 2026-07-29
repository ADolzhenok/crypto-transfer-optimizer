package com.vda.cryptotransferoptimizer.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TransferResponse {

    private String currencyPair;
    private String orderType;
    private BigDecimal orderTypeFee;
    private BigDecimal fiatAmount;
    private BigDecimal price;
    private TradeResult tradeResult;
    private List<WithdrawalResult> withdrawalResult;

}
