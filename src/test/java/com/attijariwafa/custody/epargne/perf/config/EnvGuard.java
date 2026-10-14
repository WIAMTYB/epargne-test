package com.attijariwafa.custody.epargne.perf.config;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

public final class EnvGuard {
    static final List<String> ALLOWED_HOST_SUFFIXES = List.of(
            "localhost",
            "127.0.0.1",
            ".apps.ocpdev.attijariwafa.net",
            ".apps.ocppprd.attijariwafa.net");
    static final Pattern CAMPAIGN_ID = Pattern.compile("[A-Z0-9-]{4,24}");

    private EnvGuard() {}

    static void verify(PerfConfig config) {
        String baseUrl = config.optional("epargne.baseUrl").orElse("");
        String host = URI.create(baseUrl).getHost();
        if (host == null || ALLOWED_HOST_SUFFIXES.stream().noneMatch(s ->
                "localhost".equals(s) || "127.0.0.1".equals(s) ? host.equals(s) : host.endsWith(s))) {
            throw new IllegalStateException("Refusing to target '" + baseUrl + "'");
        }
        config.optional("perf.campaignId").ifPresent(id -> {
            if (!CAMPAIGN_ID.matcher(id).matches()) {
                throw new IllegalStateException("Campaign id '" + id + "' must match " + CAMPAIGN_ID.pattern());
            }
        });
    }

    public static boolean allowedHost(String host) {
        if (host == null) return false;
        return "localhost".equals(host) || "127.0.0.1".equals(host)
                || host.endsWith(".apps.ocpdev.attijariwafa.net")
                || host.endsWith(".apps.ocppprd.attijariwafa.net");
    }

    public static boolean validCampaignId(String id) {
        return id != null && CAMPAIGN_ID.matcher(id).matches();
    }
}
