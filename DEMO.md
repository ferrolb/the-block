# Demo and interview preparation

## Before screen sharing

- Follow the root README from a clone and start the debug app.
- Use the verified Pixel 7 / API 36 emulator, with network access for placeholder images.
- Reset in-memory bids before rehearsing the script:

```sh
adb shell am force-stop com.lagniappe.theblock
adb shell am start -n com.lagniappe.theblock/.MainActivity
```

- Keep the IDE, this file, and the test results available.
- The photos are the challenge's placeholders. Do not describe them as actual vehicle photos.
- Inventory comes from a bundled fixture; turning off networking does not demonstrate
  an inventory load error. Loading/error/not-found states are exercised in focused tests.

## Five-minute buyer demo

| Time | Action | What to point out |
| --- | --- | --- |
| 0:00–0:45 | Open inventory, show 200 vehicles, search `ford 2023`, then clear. | Search is case-insensitive and supports multiple fields/terms. |
| 0:45–1:15 | Search `zzzz-no-match`, show the no-match message, clear. | Empty results are distinct from failures. |
| 1:15–2:30 | Search `A-0001`, open the Mazda, switch photos, scroll through details. | Specs, selectable VIN, condition, damage notes, dealer, auction prices; synthetic schedule, no invented countdown. |
| 2:30–3:15 | Place bid; try `21099.99`, then `21100.25`; choose Review bid. | Validation uses exact cents; the initial next minimum is CAD 21,100. Nothing is submitted during review. |
| 3:15–4:00 | Confirm, show Bid placed, choose Done. | One accepted bid updates price and count; an accepted bid is not a completed purchase. |
| 4:00–4:30 | Return to inventory. | The Mazda now shows 21,100.25 and 12 bids from the same repository state. |
| 4:30–5:00 | Reopen, review another bid, edit/cancel. | Cancellation does not change the price or count. Briefly note demo persistence limits. |

If the app has already accepted bids, force-stop/relaunch before expecting the exact
numbers above. Do not clear app storage or uninstall just to reset demo bids.

For damage notes and a missing reserve, search `A-0002` (Chevrolet Equinox).
For rotation, keep a search active or select a photo and rotate; return with toolbar
or system Back to show preserved navigation/search.

## Architecture discussion

Open these files in this order:

1. `vehicle/domain/.../model/BidRules.kt`: first/subsequent minimums, reserve behavior,
   integer money, overflow limits.
2. `vehicle/domain/.../usecase/`: observation, multi-term search, detail lookup,
   explicit load/retry, bid submission boundary.
3. `vehicle/data/.../repository/DefaultVehicleRepository.kt`: one observable inventory,
   cancellation/retry handling, validation plus mutation under the same mutex.
4. `vehicle/data/.../remote/DemoInventoryInterceptor.kt` and the DTO mapper:
   transparent demo transport and isolation of JSON details from domain models.
5. `vehicle/ui/.../details/VehicleDetailsViewModel.kt`: review/confirm/submitting/success
   states, stale-minimum recovery, duplicate submission guard.
6. `app/.../navigation/TheBlockNavHost.kt` and `VehicleDestination.kt`: typed IDs,
   entry-scoped ViewModels, back-stack restoration.
7. `VehicleBiddingTest.kt`, repository concurrency tests, and amount parser tests:
   show evidence for behavior rather than only a happy-path demo.

Package prefixes above are `com.lagniappe.theblock.vehicle` for feature modules and
`com.lagniappe.theblock` for the app. Full architecture notes are in
[vehicle/README.md](vehicle/README.md).

## Decisions to be ready to explain

- Why a native Android buyer flow fits the challenge; the desktop wording was
  interpreted as larger Android windows, with no browser implementation.
- Why Retrofit/OkHttp are present for a fixture: an explicit replaceable transport
  boundary and familiar tooling, with acknowledged overhead versus a JSON reader.
- Why data and UI depend on domain, and why Hilt wiring lives at the app boundary.
- Why a shared repository owns bid state and why the use case does not prevalidate
  an unlocked snapshot.
- Why drafts require explicit confirmation and are never automatically resubmitted
  after process recreation.
- Why no Room, backend, authentication, pagination, payments, or live countdowns
  were added within the prototype scope.
- How AI was used: planning, code, tests, and documentation were developed
  iteratively; explain the code and identify decisions you reviewed personally.
- Give an honest time estimate if asked. The repository does not record elapsed
  hands-on time and does not claim completion in four hours.

## With more time

Prioritize server-authoritative, authenticated bids with idempotency and conflict
responses, durable state, real photos, and broader accessibility/device coverage.
Then consider a dedicated large-screen list/detail layout and CI on more Android
API levels. A mutex in this prototype only coordinates one app process; it is
not a replacement for server-side auction validation.

The original interview format is in [WALKTHROUGH.md](WALKTHROUGH.md).
