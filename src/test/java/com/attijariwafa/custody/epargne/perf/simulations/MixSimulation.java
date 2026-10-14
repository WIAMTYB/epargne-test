package com.attijariwafa.custody.epargne.perf.simulations;

import com.attijariwafa.custody.epargne.perf.data.InstrumentCatalog;
import com.attijariwafa.custody.epargne.perf.scenarios.PlanLifecycleScenario;
import com.attijariwafa.custody.epargne.perf.support.CampaignContext;
import com.attijariwafa.custody.epargne.perf.support.Thresholds;
import com.attijariwafa.custody.epargne.perf.support.Workloads;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.Simulation;
import static com.attijariwafa.custody.epargne.perf.protocol.EpargneProtocol.epargne;

public class MixSimulation extends Simulation {
    {
        InstrumentCatalog.load();
        CampaignContext.start("Mix");
        setUp(
                PlanLifecycleScenario.reads().injectOpen(Workloads.rampThenHold(Workloads.profileRate() * 0.7).toArray(new OpenInjectionStep[0])),
                PlanLifecycleScenario.writesOneShot().injectOpen(Workloads.rampThenHold(Workloads.profileRate() * 0.3).toArray(new OpenInjectionStep[0]))
        ).protocols(epargne()).assertions(Thresholds.longRun());
    }
    @Override public void after() { CampaignContext.end(); }
}
