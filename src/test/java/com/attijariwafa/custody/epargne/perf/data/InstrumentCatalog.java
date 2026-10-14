package com.attijariwafa.custody.epargne.perf.data;

import com.attijariwafa.custody.epargne.perf.config.PerfConfig;
import com.attijariwafa.custody.epargne.perf.support.CampaignContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class InstrumentCatalog {
    public record Instrument(String isin, String name, String minAmount) {}
    private static final List<Instrument> INSTRUMENTS = read();
    private InstrumentCatalog() {}
    public static void load() { INSTRUMENTS.size(); }
    public static List<Instrument> instruments() { return INSTRUMENTS; }
    public static Instrument pick() { return INSTRUMENTS.get(CampaignContext.nextInt(INSTRUMENTS.size())); }
    private static List<Instrument> read() {
        try {
            List<String> lines = Files.readAllLines(PerfConfig.get().instrumentsFile(), StandardCharsets.UTF_8);
            List<Instrument> out = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).strip();
                if (line.isEmpty()) continue;
                String[] c = line.split(",", -1);
                out.add(new Instrument(c[0].strip(), c[1].strip(), c.length > 2 ? c[2].strip() : "100"));
            }
            if (out.isEmpty()) throw new IllegalStateException("No instruments in CSV");
            return List.copyOf(out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
