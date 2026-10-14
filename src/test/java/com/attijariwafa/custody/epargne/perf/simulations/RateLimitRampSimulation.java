package com.attijariwafa.custody.epargne.perf.simulations;

import com.attijariwafa.custody.epargne.perf.config.PerfConfig;
import com.attijariwafa.custody.epargne.perf.data.InstrumentCatalog;
import com.attijariwafa.custody.epargne.perf.scenarios.PlanLifecycleScenario;
import com.attijariwafa.custody.epargne.perf.support.CampaignContext;
import com.attijariwafa.custody.epargne.perf.support.Thresholds;
import com.attijariwafa.custody.epargne.perf.support.Workloads;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.Simulation;
import java.time.Duration;
import static com.attijariwafa.custody.epargne.perf.protocol.EpargneProtocol.epargne;

public class RateLimitRampSimulation extends Simulation {
    {
        InstrumentCatalog.load();
        CampaignContext.start("RateLimitRamp");
        PerfConfig config = PerfConfig.get();
        double step = config.targetRate().orElse(0.5);
        int levels = config.intValue("perf.levels", 6);
        setUp(PlanLifecycleScenario.writesOneShot()
                .injectOpen(Workloads.capacityRamp(step, levels, Duration.ofMinutes(1)).toArray(new OpenInjectionStep[0])))
                .protocols(epargne())
                .assertions(Thresholds.capacity());
    }
    @Override public void after() { CampaignContext.end(); }
}
