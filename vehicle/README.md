# Vehicle feature

The dependency direction is `vehicle:data -> vehicle:domain`; UI consumers depend on
the domain contract. Hilt provides one application-scoped repository through
`VehicleModule` in the app module, using Dagger/Hilt with KSP code generation.
`TheBlockApplication` is annotated with `@HiltAndroidApp` and `MainActivity` with
`@AndroidEntryPoint`. Hilt provides the domain use cases to `InventoryViewModel`
in `vehicle:ui`; the inventory screen is the app's launch screen.
The data and domain modules remain independent of the DI framework.

## Demo data flow

`VehicleApi -> Retrofit -> DemoInventoryInterceptor -> bundled vehicles.json`

Responses are decoded with Kotlin serialization and mapped from internal DTOs to
domain models. The interceptor only serves `GET https://vehicle-demo.invalid/vehicles`.
Other requests return 404 without network access. No backend, credentials, or server
setup is needed.

Gradle packages the original `data/vehicles.json` as a Java resource in
`vehicle:data`, and the app includes that module. There is no copied fixture to
keep synchronized. Inventory loading and mapping run off the main thread.

## Domain use cases

- `LoadInventoryUseCase()` explicitly loads inventory or retries a failure.
- `ObserveVehiclesUseCase(query)` observes inventory and filters successful results.
  Search ignores case, trims whitespace, and requires every whitespace-separated
  term to match make, model, year, VIN, or lot number. Blank searches return all
  vehicles in source order; no matches return an empty successful result.
- `ObserveVehicleUseCase(id)` observes a vehicle by exact ID. It distinguishes
  not-loaded, loading, failure, not-found, and ready states.
- `PlaceBidUseCase(id, amount)` submits a bid. Domain `BidRules` determine the
  minimum price; the repository invokes them inside its mutex against current
  state, preventing stale-price validation.

Observation does not trigger loading. A ViewModel should collect an observation
flow and launch `LoadInventoryUseCase` separately; a retry calls the same load use
case. Changing the search query creates a new observation flow (for example,
using `flatMapLatest` on query state). Inventory and detail observers receive bid
updates from the same repository. Domain use cases require neither Android nor DI.

## Inventory UI

The Material 3 inventory screen displays vehicle cards with photos, trim, mileage,
location, lot number, current/starting bid, and bid count. Search filters locally
as you type; clear search restores the full list. Loading, retryable errors,
empty inventory, and no search matches have distinct states.

`InventoryViewModel` uses the domain load/search use cases, retains the query in
`SavedStateHandle`, and exposes a StateFlow collected with lifecycle awareness.
Cards have stable vehicle ID keys and the lazy list restores its scroll state
across activity recreation. The screen applies system-bar and IME insets through
its Scaffold; the search keyboard action dismisses focus.

Coil loads the supplied placeholder URLs, including SVG responses. Images have
a local fallback, and failed image requests do not block inventory browsing.
The app needs Internet access for images; the inventory itself remains bundled.

## Vehicle details

Selecting a card opens complete vehicle details, including:

- Swipeable photos with previous/next controls and a photo counter.
- Year, make, model, trim, lot, and selectable VIN.
- Engine, transmission, drivetrain, fuel type, mileage, body style, and colours.
- Condition grade, report, title status, and all damage notes.
- Selling dealership and location.
- Starting/current bid, bid count, reserve price/status, buy-now price, and
  the supplied local auction start time.

Empty photos, no reported damage notes, no bids, and missing prices have explicit
messages. Image failures use a local fallback. Photo and scroll position survive
activity recreation. No live countdown or invented auction end time is shown.

`VehicleDetailsViewModel` decodes the typed route from `SavedStateHandle` at
the Hilt boundary, observes the current vehicle through `ObserveVehicleUseCase`,
and loads inventory if needed after process recreation. The screen handles
loading, retryable failures, and unknown IDs independently.
Bid entry will be implemented in the next phase.

## Type-safe navigation

The app's `TheBlockNavHost` uses Navigation Compose with Kotlin-serializable
`InventoryDestination` and `VehicleDetailsDestination(vehicleId)` routes defined
in `vehicle:ui`. Card clicks navigate by ID; the details ViewModel reconstructs
arguments with `SavedStateHandle.toRoute<VehicleDetailsDestination>()`. Screens
receive callbacks rather than a NavController. Navigation scopes each ViewModel
to its back-stack entry, preserving search when returning from details.

The details destination has toolbar and system back support. No vehicle snapshot
is passed through navigation arguments; details observe the shared repository.

The device navigation test verifies the selected ID, destination restoration after
activity recreation, and retained inventory search after toolbar and system back.

## Prototype assumptions

- Prices are CAD; fixture whole dollars become integer cents in the domain.
- A first bid must meet the starting price. Later bids must meet both the starting
  price and the current bid plus CAD $100. Reserve does not prevent bid acceptance.
- Bidding is simulated locally. A mutex protects validation and mutation together.
- Accepted bids are emitted to all observers and persist for the repository's
  lifetime only. They reset after process restart; nothing is sent to a server.
- Loading is idempotent after success, so screen recreation cannot reset bids.
  Failed loads can be retried. Cancellation is propagated and restores prior state.
- Auction timestamps retain their original local ISO text. No timezone or auction
  end time is invented; scheduling does not restrict demo bidding.
- Image URLs are preserved and displayed as supplied; they are placeholder images,
  not photographs of the vehicles.

## Verification

Run `./gradlew :vehicle:domain:test :vehicle:data:test :vehicle:ui:testDebugUnitTest :app:assembleDebug`.
With an emulator or device connected, run `./gradlew :app:connectedDebugAndroidTest`.

Tests cover the real 200-vehicle resource through OkHttp/Retrofit/mapping, nullable
prices, failed loads and retry, invalid data, cancellation, bid validation,
observable bid updates, and concurrent submissions. Domain tests cover search
fields, multi-term matching, state propagation, detail lookup, and bid boundaries.
An integration test exercises load, search, detail observation, and bid submission
together against the demo repository without any Android or UI dependencies.
ViewModel tests cover search, restored queries, failed loads/retry, overlapping
loads, and observable inventory updates. Device tests cover loading/error/empty
screens and browsing/searching the bundled inventory across activity recreation.
Details tests also cover selected IDs, fresh loading, retry, shared bid updates,
photo restoration, specs, condition, damage notes, dealership, auction fields,
missing prices, and not-found states.
