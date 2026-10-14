package com.attijariwafa.custody.epargne.perf.simulations;

import com.attijariwafa.custody.epargne.perf.data.InstrumentCatalog;
import com.attijariwafa.custody.epargne.perf.scenarios.PlanLifecycleScenario;
import com.attijariwafa.custody.epargne.perf.support.CampaignContext;
import com.attijariwafa.custody.epargne.perf.support.Thresholds;
import io.gatling.javaapi.core.Simulation;
import java.time.Duration;
import static com.attijariwafa.custody.epargne.perf.protocol.EpargneProtocol.epargne;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;

public class SmokeSimulation extends Simulation {
    {
        InstrumentCatalog.load();
        CampaignContext.start("Smoke");
        setUp(PlanLifecycleScenario.smoke(3, Duration.ofSeconds(2)).injectOpen(atOnceUsers(2)))
                .protocols(epargne())
                .assertions(Thresholds.smoke());
    }
    @Override public void after() { CampaignContext.end(); }
}
