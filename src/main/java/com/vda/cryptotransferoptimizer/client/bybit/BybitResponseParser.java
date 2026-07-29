package com.vda.cryptotransferoptimizer.client.bybit;

import com.vda.cryptotransferoptimizer.model.CoinData;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;

@Component
public class BybitResponseParser {

    public CoinData parse(String pair, JsonNode jsonNode){
        JsonNode coin = jsonNode
                .path("result")
                .path("list")
                .get(0);

        return CoinData.builder()
                .coinName(pair)
                .price(new BigDecimal(coin.path("lastPrice").asString()))
                .build();
    }

}
