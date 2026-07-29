package com.vda.cryptotransferoptimizer.client.bybit;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Component
public class BybitClient {

    private final WebClient WEBCLIENT;

    public BybitClient() {
        WEBCLIENT = WebClient
                .builder()
                .baseUrl("https://api.bybit.com/v5/market/tickers")
                .build();
    }

    public JsonNode getPairData(String pair) {

        return WEBCLIENT.get()
                .uri("?category=spot&symbol={pair}", pair)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

//    public List<CoinData> getPairData(String pair){
//        return WEBCLIENT.get()
//                .uri("&symbol={pair}", pair)
//                .retrieve()
//                .bodyToMono(BybitMarketData.class)
//                .map(this::toCoinData)
//                .block();
    //}
}
