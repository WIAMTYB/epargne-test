package com.attijariwafa.custody.epargne.perf.support;

import com.attijariwafa.custody.epargne.perf.config.PerfConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public final class CampaignContext {
    private static final Random RANDOM = new Random(PerfConfig.get().seed());
    private static final AtomicLong USERS = new AtomicLong();
    private CampaignContext() {}
    public static synchronized int nextInt(int bound) { return RANDOM.nextInt(bound); }
    public static long nextUserIndex() { return USERS.incrementAndGet(); }
    public static void start(String simulation) {
        try {
            Files.createDirectories(PerfConfig.get().runDir());
            String json = "{\n  \"campaignId\": \"" + PerfConfig.get().campaignId()
                    + "\",\n  \"simulation\": \"" + simulation
                    + "\",\n  \"env\": \"" + PerfConfig.get().env()
                    + "\",\n  \"baseUrl\": \"" + PerfConfig.get().baseUrl()
                    + "\",\n  \"startedAt\": \"" + Instant.now()
                    + "\"\n}\n";
            Files.writeString(PerfConfig.get().runDir().resolve("campaign.json"), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    public static void end() {
        try {
            Files.writeString(PerfConfig.get().runDir().resolve("campaign-end.json"),
                    "{\"endedAt\":\"" + Instant.now() + "\"}\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
