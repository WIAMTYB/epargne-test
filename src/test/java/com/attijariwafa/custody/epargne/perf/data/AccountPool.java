package com.attijariwafa.custody.epargne.perf.data;

import com.attijariwafa.custody.epargne.perf.config.PerfConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AccountPool {
    public record Account(String secAccount, String cashAccount, String label) {}
    private static final List<Account> ACCOUNTS = read();
    private AccountPool() {}
    public static int size() { return ACCOUNTS.size(); }
    public static List<Map<String, Object>> feederRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Account account : ACCOUNTS) {
            Map<String, Object> row = new HashMap<>();
            row.put("secaccount", account.secAccount());
            row.put("cashaccount", account.cashAccount());
            row.put("accountLabel", account.label());
            rows.add(row);
        }
        return rows;
    }
    private static List<Account> read() {
        try {
            List<String> lines = Files.readAllLines(PerfConfig.get().accountsFile(), StandardCharsets.UTF_8);
            List<Account> accounts = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).strip();
                if (line.isEmpty()) continue;
                String[] c = line.split(",", -1);
                if (c.length >= 4 && "false".equalsIgnoreCase(c[3].strip())) continue;
                accounts.add(new Account(c[0].strip(), c[1].strip(), c[2].strip()));
            }
            if (accounts.isEmpty()) throw new IllegalStateException("No accounts in CSV");
            return List.copyOf(accounts);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
