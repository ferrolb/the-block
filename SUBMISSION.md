# The Block — submission summary

An Android buyer-side auction prototype built with Kotlin and Jetpack Compose.

- Browse/search the supplied 200-vehicle inventory.
- Open complete details with photos, specs, condition, damage, dealer, and auction data.
- Enter, validate, review, confirm, or cancel a demo bid.
- See accepted bid prices and counts update on details and inventory.

## Reviewer entry points

- [README.md](README.md): prerequisites, setup, build/run/test commands, architecture,
  assumptions, tradeoffs, AI assistance, and future improvements.
- [docs/VERIFICATION.md](docs/VERIFICATION.md): clean-clone build/install evidence,
  45 JVM tests, 13 device tests, additional device checks, and coverage limits.
- [DEMO.md](DEMO.md): five-minute demo, reset steps, and interview/code walkthrough.
- [vehicle/README.md](vehicle/README.md): feature implementation notes.
- [CHALLENGE.md](CHALLENGE.md): the original OPENLANE challenge brief.
- [WALKTHROUGH.md](WALKTHROUGH.md): the supplied interview format.

## Scope

This is a native Android prototype with bundled inventory and simulated,
in-memory bidding. There is no backend service, database, authentication,
payment flow, or browser client. Bids reset after process restart. Real bidding
would require server-authoritative validation and durable, idempotent updates.

AI assistance was used throughout the staged implementation and verification.
Exact hands-on time is not recorded in the repository; no four-hour completion
claim is made.

The repository is prepared for review; publishing a fork or sending its URL is a
separate submission action.
