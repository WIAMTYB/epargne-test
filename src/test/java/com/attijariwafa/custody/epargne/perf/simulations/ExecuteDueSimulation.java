package com.attijariwafa.custody.epargne.perf.simulations;

import com.attijariwafa.custody.epargne.perf.data.InstrumentCatalog;
import com.attijariwafa.custody.epargne.perf.scenarios.PlanLifecycleScenario;
import com.attijariwafa.custody.epargne.perf.support.CampaignContext;
import com.attijariwafa.custody.epargne.perf.support.Thresholds;
import io.gatling.javaapi.core.Simulation;
import static com.attijariwafa.custody.epargne.perf.protocol.EpargneProtocol.epargne;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;

public class ExecuteDueSimulation extends Simulation {
    {
        InstrumentCatalog.load();
        CampaignContext.start("ExecuteDue");
        setUp(PlanLifecycleScenario.executeDue().injectOpen(atOnceUsers(1)))
                .protocols(epargne())
                .assertions(Thresholds.capacity());
    }
    @Override public void after() { CampaignContext.end(); }
}
