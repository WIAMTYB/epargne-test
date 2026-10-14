# Tests de performance — épargne programmée

Outil **Gatling** (comme `test-perf-main` de l'autre développeur), ciblant **l'API épargne** (`last-recette-main`).
But : voir quand le **rate limiter** sortant vers **api-titres** décroche (HTTP 429 `EXTERNAL_SERVICE_RATE_LIMITED`).

Ce n'est pas l'application métier. Tu lances ce projet **depuis ton PC** pendant que l'API épargne tourne (local ou recette).

## Ce qui est bombardé

| Simulation | Appels épargne | Appels api-titres derrière |
|---|---|---|
| Smoke | create, get, patch, pause, resume, history, executions, cancel | catalogue comptes + OPCVM |
| PlanWrite | create + cancel en boucle | le plus de trafic catalogue |
| PlanRead | exists, list, stats | peu ou pas d'api-titres |
| ExecuteDue | POST execute-due | catalogue + solde + **ordres SR** |
| RateLimitRamp | create de plus en plus vite | pour trouver le plafond `max-calls-per-minute` |
| Mix | lectures + écritures | mélange |

## Prérequis

- JDK 21+, Maven
- API épargne démarrée
- `.env` copié de `.env.example` avec `PERF_API_KEY`

## Lancer

```bash
cd C:\Users\HP\Downloads\epargne-opcvm-perf
copy .env.example .env
```

Édite `.env` : ta clé `X-Api-Key`.

Smoke (2 clients, 3 cycles create→cancel) :

```bash
mvn gatling:test -Dgatling.simulationClass=com.attijariwafa.custody.epargne.perf.simulations.SmokeSimulation -Dperf.env=local -Dperf.campaignId=EPG-SMOKE-01
```

Rampe rate limiter :

```bash
mvn gatling:test -Dgatling.simulationClass=com.attijariwafa.custody.epargne.perf.simulations.RateLimitRampSimulation -Dperf.env=local -Dperf.campaignId=EPG-RAMP-01 -Dperf.targetRate=0.5 -Dperf.levels=6
```

Batch (attention : vraies exécutions / ordres SR en recette) :

```bash
mvn gatling:test -Dgatling.simulationClass=com.attijariwafa.custody.epargne.perf.simulations.ExecuteDueSimulation -Dperf.env=local -Dperf.campaignId=EPG-DUE-01
```

Rapport HTML : `target/gatling/.../index.html`

À regarder : requêtes `create` en **429**, et sur l'API le compteur `epargne_external_service_rate_limited_total`.

Comptes / ISIN : `data/local/` ou `data/recette/` — à aligner sur tes vrais comptes titres.
