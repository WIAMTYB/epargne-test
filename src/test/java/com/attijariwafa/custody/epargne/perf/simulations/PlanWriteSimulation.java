package com.attijariwafa.custody.epargne.perf.simulations;

import com.attijariwafa.custody.epargne.perf.data.InstrumentCatalog;
import com.attijariwafa.custody.epargne.perf.scenarios.PlanLifecycleScenario;
import com.attijariwafa.custody.epargne.perf.support.CampaignContext;
import com.attijariwafa.custody.epargne.perf.support.Thresholds;
import com.attijariwafa.custody.epargne.perf.support.Workloads;
import io.gatling.javaapi.core.ClosedInjectionStep;
import io.gatling.javaapi.core.Simulation;
import static com.attijariwafa.custody.epargne.perf.protocol.EpargneProtocol.epargne;

public class PlanWriteSimulation extends Simulation {
    {
        InstrumentCatalog.load();
        CampaignContext.start("PlanWrite");
        setUp(PlanLifecycleScenario.writes()
                .injectClosed(Workloads.oneUserPerAccount().toArray(new ClosedInjectionStep[0])))
                .protocols(epargne())
                .assertions(Thresholds.writes());
    }
    @Override public void after() { CampaignContext.end(); }
}
