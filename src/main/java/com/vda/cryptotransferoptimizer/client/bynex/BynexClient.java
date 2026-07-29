package com.vda.cryptotransferoptimizer.client.bynex;

import com.vda.cryptotransferoptimizer.dto.external.BynexMarketData;
import com.vda.cryptotransferoptimizer.dto.external.PairData;
import com.vda.cryptotransferoptimizer.dto.request.CalculateRequest;
import com.vda.cryptotransferoptimizer.model.CoinData;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.net.http.HttpHeaders;
import java.util.List;

@Component
public class BynexClient {

    private final WebClient WEBCLIENT;

    public BynexClient() {
        WEBCLIENT = WebClient
                .builder()
                .baseUrl("https://bynex.io")
                .build();
    }

    public List<CoinData> getMarketData(){
        return WEBCLIENT.get()
                .uri("/trading/ru/api/rate/market")
                .retrieve()
                .bodyToFlux(BynexMarketData.class)
                .map(this::toCoinData)
                .collectList()
                .block();
    }

    public CoinData getPairData(CalculateRequest request){
        return WEBCLIENT.get()
                .uri(
                        "/trading/ru/api/rate/{coinName}-{fiatName}",
                        request.getCoinName(),
                        request.getFiatName()
                )
                .retrieve()
                .bodyToMono(PairData.class)
                .map(this::toCoinData)
                .block();
    }

    public JsonNode getFeesJson(String jsonBuildId){
        return WEBCLIENT.get()
                .uri("/_next/data/{buildId}/ru/fees.json", jsonBuildId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    public String getHtmlForFeesJson(){
        return WEBCLIENT.get()
                .uri("/ru/fees")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private CoinData toCoinData(BynexMarketData marketData){
        String[] coinAndFiatNames = marketData.getCode().split("-");
        return CoinData.builder()
                .coinName(coinAndFiatNames[0])
                .fiatName(coinAndFiatNames[1])
                .price(marketData.getPrice())
                .build();
    }

    private CoinData toCoinData(PairData pairData){
        String[] coinAndFiatNames = pairData.getPair().split("-");
        return CoinData.builder()
                .coinName(coinAndFiatNames[0])
                .fiatName(coinAndFiatNames[1])
                .price(pairData.getPrice())
                .build();
    }

}
