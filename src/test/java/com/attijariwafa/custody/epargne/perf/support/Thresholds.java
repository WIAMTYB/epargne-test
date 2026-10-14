package com.attijariwafa.custody.epargne.perf.support;

import io.gatling.javaapi.core.Assertion;
import java.util.List;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.global;

public final class Thresholds {
    public static final String EXISTS = "exists";
    public static final String CREATE = "create";
    public static final String CREATE_RATE_LIMITED = "create-rate-limited";
    public static final String GET = "get";
    public static final String LIST = "list";
    public static final String STATS = "stats";
    public static final String UPDATE = "update";
    public static final String PAUSE = "pause";
    public static final String RESUME = "resume";
    public static final String CANCEL = "cancel";
    public static final String HISTORY = "history";
    public static final String EXECUTIONS = "executions";
    public static final String EXECUTE_DUE = "execute-due";

    private Thresholds() {}

    public static List<Assertion> smoke() {
        return List.of(
                details(CREATE).failedRequests().percent().lt(5.0),
                global().failedRequests().percent().lt(5.0));
    }
    public static List<Assertion> writes() {
        return List.of(
                details(CREATE).responseTime().percentile(95).lt(2_000),
                global().failedRequests().percent().lt(10.0));
    }
    public static List<Assertion> reads() {
        return List.of(
                details(LIST).responseTime().percentile(95).lt(1_000),
                global().failedRequests().percent().lt(2.0));
    }
    public static List<Assertion> longRun() {
        return List.of(global().failedRequests().percent().lt(15.0));
    }
    public static List<Assertion> capacity() {
        return List.of(global().successfulRequests().count().gt(0L));
    }
}
