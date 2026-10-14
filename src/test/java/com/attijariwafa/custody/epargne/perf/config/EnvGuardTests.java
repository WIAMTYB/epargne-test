package com.attijariwafa.custody.epargne.perf.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EnvGuardTests {
    @Test
    void onlyTestHostsAreAllowed() {
        assertThat(EnvGuard.allowedHost("localhost")).isTrue();
        assertThat(EnvGuard.allowedHost("recette-epargne-opcvm-services.apps.ocpdev.attijariwafa.net")).isTrue();
        assertThat(EnvGuard.allowedHost("epargne.attijariwafa.net")).isFalse();
    }

    @Test
    void campaignIdsAreShortUpperCaseTokens() {
        assertThat(EnvGuard.validCampaignId("EPG-SMOKE-01")).isTrue();
        assertThat(EnvGuard.validCampaignId("abc")).isFalse();
    }
}
