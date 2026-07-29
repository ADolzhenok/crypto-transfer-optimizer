package com.vda.cryptotransferoptimizer.client.bynex;

import com.vda.cryptotransferoptimizer.model.WithdrawalNetworksInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BynexFeeResponseParser {

    private static final Pattern COIN_PATTERN = Pattern.compile("<em>([^<(]+)(?:\\s*\\((.*?)\\))?</em>");
    private static final Pattern FEE_PATTERN = Pattern.compile("([0-9.]*)\\s(.*)");

    public List<WithdrawalNetworksInfo> parsePage(String coinName, JsonNode jsonNode){
        JsonNode table = extractTable(jsonNode);
        List<WithdrawalNetworksInfo> withdrawalNetworksInfoList = new ArrayList<>();
        for (int i = 1; i < table.size(); i++){
            JsonNode row = table.get(i);
            String currencyRaw = row.get(0).toString();
            if (!currencyRaw.contains(coinName)){
                continue;
            }
            String feeRaw = row.get(3).toString();
            String network = parseNetwork(currencyRaw);
            BigDecimal fee = parseFee(feeRaw);

            withdrawalNetworksInfoList.add(
                    WithdrawalNetworksInfo.builder()
                            .coinName(coinName)
                            .fee(fee)
                            .blockchainNetwork(network)
                            .build()
            );
        }

        return withdrawalNetworksInfoList;

    }

    private JsonNode extractTable(JsonNode jsonNode){
        return jsonNode.path("pageProps")
                .path("page")
                .path("data")
                .get(0)
                .path("tabs")
                .get(1)
                .path("body")
                .get(0)
                .path("body");

    }

    private String parseNetwork(String currencyRaw){
        Matcher matcher = COIN_PATTERN.matcher(currencyRaw);
        if (matcher.find()){
            String network = matcher.group(2);
            return network;
        }
        return "";
    }

    private BigDecimal parseFee(String feeRaw){
        Matcher matcher = FEE_PATTERN.matcher(feeRaw);
        if (matcher.find()){
            BigDecimal fee = new BigDecimal(matcher.group(1));
            return fee;
        }
        return new BigDecimal("");
    }

    public String parseHtml(String html){
        Document document = Jsoup.parse(html);
        return document
                .select("script[src*=_buildManifest.js]")
                .first()
                .attr("src")
                .split("/")[3];

    }

}
