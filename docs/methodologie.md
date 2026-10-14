# Méthodologie

Cible : API épargne programmée (`last-recette-main`).
Le limiteur testé est **sortant** : `clients.api-titres.max-calls-per-minute` (Resilience4j, par pod).

Un create = 2 appels api-titres (relation + fonds).
Un execute-due = N × (catalogue + solde + ordre).

Les 429 HTTP de l'épargne portent le code `EXTERNAL_SERVICE_RATE_LIMITED`.
Les lectures (list/stats) ne saturent presque pas api-titres.

Pas de profil production : localhost, ocpdev, ocppprd seulement.
