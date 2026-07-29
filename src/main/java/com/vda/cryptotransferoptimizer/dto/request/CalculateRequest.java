package com.vda.cryptotransferoptimizer.dto.request;

import com.vda.cryptotransferoptimizer.enums.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
public class CalculateRequest {

    @NotNull
    private String fiatName;
    @NotNull
    private String coinName;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;
    //TODO Realization MAKER calculating
    // @NotNull
    // private OrderType orderType;

}
