package com.attijariwafa.custody.epargne.perf.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class PerfConfig {
    private static final PerfConfig INSTANCE = load();
    private final Map<String, String> values;
    private final Path projectDir;

    private PerfConfig(Map<String, String> values, Path projectDir) {
        this.values = values;
        this.projectDir = projectDir;
    }

    public static PerfConfig get() { return INSTANCE; }

    private static PerfConfig load() {
        Path projectDir = Path.of(System.getProperty("perf.projectDir", System.getProperty("user.dir")))
                .toAbsolutePath().normalize();
        Map<String, String> dotEnv = readDotEnv(projectDir.resolve(".env"));
        String env = firstNonBlank(System.getProperty("perf.env"), System.getenv("PERF_ENV"), dotEnv.get("PERF_ENV"), "local");
        Map<String, String> values = new HashMap<>(readProfile(env));
        values.put("perf.env", env);
        copy(dotEnv, values);
        copy(System.getenv(), values);
        System.getProperties().forEach((k, v) -> {
            String key = k.toString();
            if (key.startsWith("perf.") && v != null && !v.toString().isBlank()) {
                values.put(key, v.toString());
            }
        });
        PerfConfig config = new PerfConfig(values, projectDir);
        EnvGuard.verify(config);
        return config;
    }

    private static void copy(Map<String, String> source, Map<String, String> target) {
        for (Map.Entry<String, String> entry : source.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) continue;
            switch (entry.getKey()) {
                case "PERF_API_KEY" -> target.put("secret.apiKey", entry.getValue());
                case "PERF_METRICS_API_KEY" -> target.put("secret.metricsApiKey", entry.getValue());
                case "PERF_CAMPAIGN_ID" -> target.put("perf.campaignId", entry.getValue());
                case "PERF_SEED" -> target.put("perf.seed", entry.getValue());
                case "PERF_RUN_DIR" -> target.put("perf.runDir", entry.getValue());
                default -> { }
            }
        }
    }

    private static Map<String, String> readProfile(String env) {
        String resource = "profiles/" + env + ".properties";
        try (InputStream in = PerfConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("Unknown environment '" + env + "'");
            Properties properties = new Properties();
            properties.load(in);
            Map<String, String> map = new HashMap<>();
            properties.forEach((k, v) -> map.put(k.toString(), v.toString().trim()));
            return map;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + resource, e);
        }
    }

    static Map<String, String> readDotEnv(Path file) {
        Map<String, String> map = new HashMap<>();
        if (!Files.exists(file)) return map;
        try {
            for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String line = raw.strip();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                int eq = line.indexOf('=');
                map.put(line.substring(0, eq).strip(), line.substring(eq + 1).strip());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }
        return map;
    }

    private static String firstNonBlank(String... candidates) {
        for (String c : candidates) if (c != null && !c.isBlank()) return c;
        return null;
    }

    public String require(String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing '" + key + "'");
        return value;
    }

    public Optional<String> optional(String key) {
        String value = values.get(key);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    public int intValue(String key, int fallback) {
        return optional(key).map(Integer::parseInt).orElse(fallback);
    }

    public String env() { return values.get("perf.env"); }
    public Path projectDir() { return projectDir; }
    public String baseUrl() { return require("epargne.baseUrl"); }

    public String apiKey() {
        String key = values.get("secret.apiKey");
        if (key == null || key.isBlank()) throw new IllegalStateException("PERF_API_KEY is not set");
        return key;
    }

    public String campaignId() { return optional("perf.campaignId").orElse("EPG-LOCAL-01"); }
    public long seed() { return Long.parseLong(optional("perf.seed").orElse("20261014")); }
    public Duration duration() { return Duration.parse(optional("perf.duration").orElse("PT10M")); }
    public Optional<Double> targetRate() { return optional("perf.targetRate").map(Double::parseDouble); }
    public Optional<Integer> users() { return optional("perf.users").map(Integer::parseInt); }
    public String profile() { return optional("perf.profile").orElse("nominal").toLowerCase(Locale.ROOT); }
    public Path runDir() { return optional("perf.runDir").map(Path::of).orElse(projectDir.resolve("target").resolve("perf-run")); }
    public Duration pace() {
        return Duration.ofSeconds(optional("perf.paceSeconds").map(Integer::parseInt).orElse(5));
    }
    public int maxPlans() {
        return optional("perf.maxPlans").map(Integer::parseInt).orElse(intValue("limits.maxPlansPerCampaign", 200));
    }
    public String planAmount() { return optional("plans.amount").orElse("500"); }
    public int preferredDay() { return intValue("plans.preferredDay", 5); }
    public String frequency() { return optional("plans.frequency").orElse("MONTHLY"); }
    public Path accountsFile() { return projectDir.resolve(require("data.accounts")); }
    public Path instrumentsFile() { return projectDir.resolve(require("data.instruments")); }
}
