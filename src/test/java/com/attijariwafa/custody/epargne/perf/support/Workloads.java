package com.attijariwafa.custody.epargne.perf.support;

import com.attijariwafa.custody.epargne.perf.config.PerfConfig;
import com.attijariwafa.custody.epargne.perf.data.AccountPool;
import io.gatling.javaapi.core.ClosedInjectionStep;
import io.gatling.javaapi.core.OpenInjectionStep;
import java.time.Duration;
import java.util.List;
import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.incrementUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;

public final class Workloads {
    public static final double NOMINAL_RATE = 2.0;
    public static final double PEAK_RATE = 10.0;
    private Workloads() {}
    public static Duration duration() { return PerfConfig.get().duration(); }
    public static List<ClosedInjectionStep> oneUserPerAccount() {
        int users = PerfConfig.get().users().map(u -> Math.min(u, AccountPool.size())).orElse(AccountPool.size());
        return List.of(constantConcurrentUsers(users).during(duration()));
    }
    public static List<OpenInjectionStep> rampThenHold(double fallbackRate) {
        double rate = PerfConfig.get().targetRate().orElse(fallbackRate);
        Duration total = duration();
        Duration ramp = total.compareTo(Duration.ofMinutes(4)) > 0 ? Duration.ofMinutes(2) : total.dividedBy(4);
        return List.of(
                rampUsersPerSec(Math.max(0.1, rate / 10)).to(rate).during(ramp),
                constantUsersPerSec(rate).during(total.minus(ramp)));
    }
    public static double profileRate() {
        return "peak".equals(PerfConfig.get().profile()) ? PEAK_RATE : NOMINAL_RATE;
    }
    public static List<OpenInjectionStep> capacityRamp(double step, int levels, Duration level) {
        return List.of(incrementUsersPerSec(step)
                .times(levels)
                .eachLevelLasting(level)
                .separatedByRampsLasting(Duration.ofSeconds(30))
                .startingFrom(step));
    }
}
