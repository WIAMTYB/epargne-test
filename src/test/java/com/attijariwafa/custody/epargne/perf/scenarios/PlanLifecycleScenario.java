package com.attijariwafa.custody.epargne.perf.scenarios;

import com.attijariwafa.custody.epargne.perf.actions.PlanActions;
import com.attijariwafa.custody.epargne.perf.config.PerfConfig;
import com.attijariwafa.custody.epargne.perf.data.AccountPool;
import com.attijariwafa.custody.epargne.perf.support.CampaignContext;
import io.gatling.javaapi.core.ScenarioBuilder;
import java.time.Duration;
import static io.gatling.javaapi.core.CoreDsl.forever;
import static io.gatling.javaapi.core.CoreDsl.listFeeder;
import static io.gatling.javaapi.core.CoreDsl.pace;
import static io.gatling.javaapi.core.CoreDsl.repeat;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public final class PlanLifecycleScenario {
    private PlanLifecycleScenario() {}

    public static ScenarioBuilder smoke(int loops, Duration paceBetween) {
        return scenario("plan-lifecycle-smoke")
                .feed(listFeeder(AccountPool.feederRows()).circular())
                .exec(session -> session.set("userIndex", CampaignContext.nextUserIndex()))
                .repeat(loops).on(pace(paceBetween).exec(PlanActions.lifecycle()));
    }

    public static ScenarioBuilder writes() {
        return scenario("plan-writes")
                .feed(listFeeder(AccountPool.feederRows()).circular())
                .exec(session -> session.set("userIndex", CampaignContext.nextUserIndex()))
                .forever().on(pace(PerfConfig.get().pace()).exec(PlanActions.writeCreateCancel()));
    }

    public static ScenarioBuilder writesOneShot() {
        return scenario("plan-writes-arrivals")
                .feed(listFeeder(AccountPool.feederRows()).circular())
                .exec(PlanActions.writeCreateCancel());
    }

    public static ScenarioBuilder reads() {
        return scenario("plan-reads")
                .feed(listFeeder(AccountPool.feederRows()).circular())
                .forever().on(PlanActions.pickInstrument().exec(PlanActions.exists()).exec(PlanActions.list()).exec(PlanActions.stats()));
    }

    public static ScenarioBuilder executeDue() {
        return scenario("execute-due").exec(PlanActions.executeDue());
    }
}
