package com.attijariwafa.custody.epargne.perf.simulations;

import com.attijariwafa.custody.epargne.perf.data.InstrumentCatalog;
import com.attijariwafa.custody.epargne.perf.scenarios.PlanLifecycleScenario;
import com.attijariwafa.custody.epargne.perf.support.CampaignContext;
import com.attijariwafa.custody.epargne.perf.support.Thresholds;
import com.attijariwafa.custody.epargne.perf.support.Workloads;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.Simulation;
import static com.attijariwafa.custody.epargne.perf.protocol.EpargneProtocol.epargne;

public class PlanReadSimulation extends Simulation {
    {
        InstrumentCatalog.load();
        CampaignContext.start("PlanRead");
        setUp(PlanLifecycleScenario.reads()
                .injectOpen(Workloads.rampThenHold(5).toArray(new OpenInjectionStep[0])))
                .protocols(epargne())
                .assertions(Thresholds.reads());
    }
    @Override public void after() { CampaignContext.end(); }
}
