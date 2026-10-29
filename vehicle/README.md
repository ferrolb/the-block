# Vehicle feature data layer

The dependency direction is `vehicle:data -> vehicle:domain`; UI consumers depend on
the domain contract. The app owns one lazily created repository in
`TheBlockApplication`. A future ViewModel should call `loadInventory()` from its
coroutine scope and collect `inventory`.

## Demo data flow

`VehicleApi -> Retrofit -> DemoInventoryInterceptor -> bundled vehicles.json`

Responses are decoded with Kotlin serialization and mapped from internal DTOs to
domain models. The interceptor only serves `GET https://vehicle-demo.invalid/vehicles`.
Other requests return 404 without network access. No backend, credentials, or server
setup is needed.

Gradle packages the original `data/vehicles.json` as a Java resource in
`vehicle:data`, and the app includes that module. There is no copied fixture to
keep synchronized. Inventory loading and mapping run off the main thread.

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

Run `./gradlew :vehicle:data:test :app:assembleDebug`.

Tests cover the real 200-vehicle resource through OkHttp/Retrofit/mapping, nullable
prices, failed loads and retry, invalid data, cancellation, bid validation,
observable bid updates, and concurrent submissions.
