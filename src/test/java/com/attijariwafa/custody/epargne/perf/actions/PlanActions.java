package com.attijariwafa.custody.epargne.perf.actions;

import com.attijariwafa.custody.epargne.perf.config.PerfConfig;
import com.attijariwafa.custody.epargne.perf.data.InstrumentCatalog;
import com.attijariwafa.custody.epargne.perf.support.Thresholds;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Session;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.doIf;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public final class PlanActions {
    private PlanActions() {}

    public static ChainBuilder pickInstrument() {
        return exec(session -> {
            InstrumentCatalog.Instrument instrument = InstrumentCatalog.pick();
            return session.set("assetCode", instrument.isin()).set("assetName", instrument.name());
        });
    }

    public static ChainBuilder exists() {
        return exec(http(Thresholds.EXISTS)
                .get("/api/v1/plans/exists")
                .queryParam("secAccount", "#{secaccount}")
                .queryParam("assetCode", "#{assetCode}")
                .check(status().is(200))
                .check(jsonPath("$.exists").saveAs("planExists"))
                .check(jsonPath("$.reference").optional().saveAs("planReference")));
    }

    public static ChainBuilder cancelIfExists() {
        return doIf(session -> {
            Object exists = session.get("planExists");
            return Boolean.TRUE.equals(exists) || "true".equalsIgnoreCase(String.valueOf(exists));
        }).then(cancel());
    }

    public static ChainBuilder create() {
        return exec(http(Thresholds.CREATE)
                .post("/api/v1/plans")
                .body(StringBody(PlanActions::createBody)).asJson()
                .check(status().in(201, 400, 429).saveAs("createStatus"))
                .check(jsonPath("$.reference").optional().saveAs("planReference"))
                .check(jsonPath("$.code").optional().saveAs("createCode")));
    }

    public static ChainBuilder get() {
        return doIf(session -> session.contains("planReference") && session.getString("planReference") != null
                && !session.getString("planReference").isBlank()).then(
                exec(http(Thresholds.GET)
                        .get("/api/v1/plans/#{planReference}")
                        .check(status().in(200, 404))));
    }

    public static ChainBuilder list() {
        return exec(http(Thresholds.LIST)
                .get("/api/v1/plans")
                .queryParam("secAccount", "#{secaccount}")
                .queryParam("size", "20")
                .check(status().is(200)));
    }

    public static ChainBuilder stats() {
        return exec(http(Thresholds.STATS).get("/api/v1/plans/stats").check(status().is(200)));
    }

    public static ChainBuilder update() {
        return doIf(PlanActions::hasReference).then(
                exec(http(Thresholds.UPDATE)
                        .patch("/api/v1/plans/#{planReference}")
                        .body(StringBody(session -> "{\"recurringAmount\":" + bumpAmount(session) + "}"))
                        .asJson()
                        .check(status().in(200, 400, 429))));
    }

    public static ChainBuilder pause() {
        return doIf(PlanActions::hasReference).then(
                exec(http(Thresholds.PAUSE).post("/api/v1/plans/#{planReference}/pause").check(status().in(200, 409, 400))));
    }

    public static ChainBuilder resume() {
        return doIf(PlanActions::hasReference).then(
                exec(http(Thresholds.RESUME).post("/api/v1/plans/#{planReference}/resume").check(status().in(200, 409, 400))));
    }

    public static ChainBuilder history() {
        return doIf(PlanActions::hasReference).then(
                exec(http(Thresholds.HISTORY).get("/api/v1/plans/#{planReference}/history").check(status().in(200, 404))));
    }

    public static ChainBuilder executions() {
        return doIf(PlanActions::hasReference).then(
                exec(http(Thresholds.EXECUTIONS).get("/api/v1/plans/#{planReference}/executions").check(status().in(200, 404))));
    }

    public static ChainBuilder cancel() {
        return doIf(PlanActions::hasReference).then(
                exec(http(Thresholds.CANCEL).post("/api/v1/plans/#{planReference}/cancel").check(status().in(200, 404, 409))));
    }

    public static ChainBuilder executeDue() {
        return exec(http(Thresholds.EXECUTE_DUE)
                .post("/api/v1/plans/execute-due")
                .queryParam("wait", "true")
                .check(status().in(200, 202, 409, 429)));
    }

    public static ChainBuilder lifecycle() {
        return pickInstrument()
                .exec(exists())
                .exec(cancelIfExists())
                .exec(create())
                .exec(get())
                .exec(update())
                .exec(pause())
                .exec(resume())
                .exec(history())
                .exec(executions())
                .exec(cancel());
    }

    public static ChainBuilder writeCreateCancel() {
        return pickInstrument().exec(exists()).exec(cancelIfExists()).exec(create()).exec(cancel());
    }

    private static boolean hasReference(Session session) {
        return session.contains("planReference")
                && session.getString("planReference") != null
                && !session.getString("planReference").isBlank();
    }

    private static String createBody(Session session) {
        PerfConfig cfg = PerfConfig.get();
        return "{"
                + "\"clientSecAccount\":\"" + session.getString("secaccount") + "\","
                + "\"clientCashAccount\":\"" + session.getString("cashaccount") + "\","
                + "\"assetCode\":\"" + session.getString("assetCode") + "\","
                + "\"byAmount\":true,"
                + "\"recurringAmount\":" + cfg.planAmount() + ","
                + "\"frequency\":\"" + cfg.frequency() + "\","
                + "\"preferredDayOfMonth\":" + cfg.preferredDay()
                + "}";
    }

    private static String bumpAmount(Session session) {
        return String.valueOf(Integer.parseInt(PerfConfig.get().planAmount()) + 50);
    }
}
