# Vehicle feature

The dependency direction is `vehicle:data -> vehicle:domain`; UI consumers depend on
the domain contract. Hilt provides one application-scoped repository through
`VehicleModule` in the app module, using Dagger/Hilt with KSP code generation.
`TheBlockApplication` is annotated with `@HiltAndroidApp` and `MainActivity` with
`@AndroidEntryPoint`. Hilt also provides the domain use cases, ready for injection
into future ViewModels.
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
- Image URLs are preserved. Image loading will be added with the UI.

## Verification

Run `./gradlew :vehicle:domain:test :vehicle:data:test :app:assembleDebug`.

Tests cover the real 200-vehicle resource through OkHttp/Retrofit/mapping, nullable
prices, failed loads and retry, invalid data, cancellation, bid validation,
observable bid updates, and concurrent submissions. Domain tests cover search
fields, multi-term matching, state propagation, detail lookup, and bid boundaries.
An integration test exercises load, search, detail observation, and bid submission
together against the demo repository without any Android or UI dependencies.
