<p align="center">
  <img src="docs/the_block_repo.png" alt="The Block vehicle auction prototype" width="960" />
</p>

# The Block — Android buyer prototype

Browse 200 vehicles, search inventory, inspect full vehicle details, and place a
confirmed demo bid. Accepted bids update both details and inventory immediately.

Built for the OPENLANE challenge with Kotlin, Jetpack Compose, Material 3,
type-safe Navigation Compose, Hilt/Dagger, coroutines/Flow, Retrofit, OkHttp,
Kotlin serialization, and Coil. The original brief is preserved in
[CHALLENGE.md](CHALLENGE.md).

## Run locally

### Prerequisites

- Android Studio compatible with **Android Gradle Plugin 9.5.0**.
- Android SDK Platform **37**, the SDK Build-Tools requested by Gradle, and
  Platform-Tools. Install/accept licenses in Android Studio's SDK Manager.
- An Android emulator or physical device running **Android 7.0 / API 24+**.
  Verification was performed on a Pixel 7 emulator running API 36.
- A JDK **17+** to launch the Gradle wrapper. The checked-in daemon JVM criteria
  select **Java 25**, which Gradle can provision automatically. First setup needs
  Internet access for Gradle 9.6.0, the daemon JDK, and Maven dependencies.

There are no API keys, accounts, backend processes, database migrations, or
environment secrets to configure.

### Android Studio

1. Clone this repository and open its root folder in Android Studio.
2. Allow Gradle sync, SDK installation, and daemon JDK provisioning to finish.
3. Start an emulator or connect a device with USB debugging enabled.
4. Select the **app** run configuration and click **Run**.

The launch screen should show **200 vehicles**.

### Command line

From the repository root, set `ANDROID_HOME` to your Android SDK directory
(or let Android Studio create an untracked `local.properties` with `sdk.dir`).

```sh
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.lagniappe.theblock/.MainActivity
```

On Windows, use `gradlew.bat` instead of `./gradlew`. For multiple connected
devices, add `-s DEVICE_SERIAL` to each adb command.

If sync reports an unsupported AGP version, update Android Studio to a compatible
version. If SDK discovery fails, check `ANDROID_HOME` or the SDK path in Android
Studio. Use the committed wrapper rather than a separately installed Gradle.

## Buyer flow

- Search make, model, year, VIN, or lot. Matching ignores case; every search term
  must match. Clear search restores the inventory.
- Tap a card for photos, specifications, condition, damage notes, dealership,
  location, and auction information.
- Tap **Place bid**, enter a CAD amount, choose **Review bid**, and explicitly
  **Confirm bid**. Editing or cancelling does not submit anything.
- After **Bid placed**, return to inventory to see the updated price and bid count.

Loading, retry, empty search/inventory, unknown vehicle, image fallback, and bid
validation states are handled. Search, navigation, and photo selection survive
activity recreation.

## Data and assumptions

Inventory is the original [data/vehicles.json](data/vehicles.json), bundled by
Gradle without a duplicate copy. A clearly named demo OkHttp interceptor serves
the inventory response through Retrofit and Kotlin serialization. No inventory
request reaches a server. Images use the supplied external placeholder URLs;
inventory and bidding remain usable when those images cannot load.

- All prices are CAD, represented as integer cents.
- First bids must meet the starting price. Later bids must also exceed the
  current bid by at least CAD $100. Bids may include cents.
- Reserve prices do not prevent bid acceptance; acceptance does not mean purchase.
- Repository validation and mutation share a mutex, so competing bids recheck
  the latest price. The form reports a changed minimum and requires another review.
- Bids exist only in the app process and reset when it restarts. This is a demo,
  not a real auction backend.
- Auction dates are synthetic local timestamps. No timezone, end time, live
  countdown, or auction closure is invented.
- Authentication, seller tools, checkout, payments, persistence, and real-time
  multi-buyer synchronization are outside scope.
- This is a native Android application, not a browser application. Wider Android
  windows use a bounded content width; a dedicated desktop/web layout is not included.

## Architecture and decisions

| Module | Responsibility |
| --- | --- |
| `app` | Application, Hilt wiring, typed navigation host |
| `vehicle:domain` | Models, repository contract, use cases, bid rules |
| `vehicle:data` | Retrofit/OkHttp demo transport, DTO mapping, shared in-memory repository |
| `vehicle:ui` | Hilt ViewModels, lifecycle-aware state, Compose screens |

Data and UI depend on domain. One application-scoped repository keeps inventory
and details consistent. Screens receive callbacks rather than a NavController;
navigation passes vehicle IDs rather than data snapshots.

Retrofit and a demo interceptor make the transport boundary explicit while
keeping the project runnable without a server. A direct JSON reader would be
simpler for this fixture-only prototype. The separate modules and use cases make
the business rules testable, with additional setup cost appropriate to discussing
the architecture in the interview. See [vehicle/README.md](vehicle/README.md) for
implementation details.

## Tests and verification

Run the JVM tests, debug lint checks, and build:

```sh
./gradlew :vehicle:domain:test :vehicle:data:test :vehicle:ui:testDebugUnitTest :app:testDebugUnitTest :app:lintDebug :vehicle:ui:lintDebug :app:assembleDebug
```

With an emulator or device connected:

```sh
./gradlew :app:connectedDebugAndroidTest
```

The suite covers fixture parsing/mapping, search, bid boundaries and concurrency,
ViewModel transitions, cancellation, typed navigation/restoration, details, and
a confirmed bid updating both details and inventory.

See [docs/VERIFICATION.md](docs/VERIFICATION.md) for the recorded checks and their
limits, and [DEMO.md](DEMO.md) for a five-minute demo and interview walkthrough.

## Workflow, time, and next improvements

The work proceeded from data/repository to use cases, inventory, navigation,
details, bidding, and verification. AI assistance was used for planning,
implementation, tests, and documentation; the code and tradeoffs are intended
to be reviewed and explained by the candidate. Exact hands-on time was not
tracked in this repository.

With more time: server-authoritative bids with idempotency/conflict handling,
durable storage, real vehicle photography, broader device/accessibility testing,
and a dedicated layout for larger screens.
