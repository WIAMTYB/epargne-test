package com.attijariwafa.custody.epargne.perf.protocol;

import com.attijariwafa.custody.epargne.perf.config.PerfConfig;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import static io.gatling.javaapi.http.HttpDsl.http;

public final class EpargneProtocol {
    private EpargneProtocol() {}
    public static HttpProtocolBuilder epargne() {
        PerfConfig config = PerfConfig.get();
        return http.baseUrl(config.baseUrl())
                .header("X-Api-Key", config.apiKey())
                .acceptHeader("application/json")
                .contentTypeHeader("application/json")
                .userAgentHeader("epargne-opcvm-performance-tests/1.0 (" + config.campaignId() + ")")
                .disableCaching()
                .disableFollowRedirect();
    }
}
