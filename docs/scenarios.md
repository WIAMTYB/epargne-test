# Scénarios

Même esprit que `test-perf-main/docs/scenarios.md`, pour l'épargne.

## Requêtes

| Nom Gatling | HTTP | api-titres derrière |
|---|---|---|
| exists | GET /api/v1/plans/exists | non |
| create | POST /api/v1/plans | comptes + catalogue OPCVM |
| get / list / stats / history / executions | GET | non (sauf assetName catalogue au GET) |
| update | PATCH | catalogue (cash si changé) |
| pause / resume / cancel | POST | non |
| execute-due | POST /api/v1/plans/execute-due | catalogue + solde + SR |

create 429 = rate limiter épargne → api-titres.

## Simulations

- Smoke : 2 users, 3 lifecycles, 2 s d'écart
- PlanWrite : 1 user par compte, create+cancel pendant `perf.duration`
- PlanRead : montée puis palier
- ExecuteDue : 1 appel batch
- RateLimitRamp : paliers +0.5 create/s
- Mix : 70 % lectures, 30 % écritures
