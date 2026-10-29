# Verification record

Verified on 2026-10-29. No implementation changes were needed during this handoff
phase; the changes are documentation.

## Clean-checkout checkpoint

Implementation revision: `967fa95ead7c0b92f81c750125902e06bd0cb765`.

A separate local Git clone was created in a temporary directory outside the
working project, without hardlinked files. It had no `local.properties`, build
outputs, or project-local Gradle state. SDK discovery used `ANDROID_HOME`.
The commands documented in the root README were exercised against that clone.

Build/configuration cache reuse was disabled:

```sh
./gradlew --no-configuration-cache --no-build-cache :vehicle:domain:test :vehicle:data:test :vehicle:ui:testDebugUnitTest :app:testDebugUnitTest :app:lintDebug :vehicle:ui:lintDebug :app:assembleDebug
```

Result: **BUILD SUCCESSFUL; 122 tasks executed**. All JVM tests and both debug lint
tasks passed, and the APK was produced. The installed SDK, downloaded daemon JDK,
and global Gradle dependency cache were reused; this was a fresh source checkout,
not a test of downloading all dependencies on a new machine.

The clean clone also ran:

```sh
./gradlew --no-configuration-cache --no-build-cache :app:connectedDebugAndroidTest
```

Result: **BUILD SUCCESSFUL** on the Pixel 7 emulator. The complete device suite
contains 13 tests. Its APK was then installed with `adb install -r` and launched
using `adb shell am start -n com.lagniappe.theblock/.MainActivity`, matching the
README instructions. Inventory displayed 200 vehicles.
The temporary clone remained clean according to `git status --short`.

This source revision predates the README/demo/report edits from the handoff phase;
the implementation tested is unchanged. The documentation is supplied in the
working repository for review and submission.

## Environment

| Item | Verified value |
| --- | --- |
| Host | macOS, x86_64 |
| Gradle wrapper | 9.6.0, checksum pinned |
| Gradle launcher | JDK 17.0.2 |
| Gradle daemon criteria | Java 25 |
| Android Gradle Plugin | 9.5.0 |
| Compile/target SDK | 37 |
| Test device | Pixel 7 emulator, Android 16 / API 36 |
| Minimum supported SDK in configuration | 24 |

## Focused coverage

| Suite | Tests | Result |
| --- | ---: | --- |
| Domain: search, observation, bid rules/bounds | 15 | Pass |
| Data: fixture/Retrofit/mapping, retry/cancellation, atomic bids, use-case integration | 13 | Pass |
| UI JVM: inventory/details ViewModels and exact-cent amount parsing | 16 | Pass |
| App JVM template smoke test | 1 | Pass |
| Device: package smoke, inventory states/search, typed navigation, details, bidding | 13 | Pass |

Total: **45 JVM tests and 13 device tests**. The basic app smoke test is included
in the JVM total; the feature tests account for 44 of those tests.

The buyer journey verifies a decimal bid, explicit review/confirmation, updated
details price/count, and the same update on inventory. It also verifies that
editing/cancelling a subsequent bid leaves the count unchanged. Repository tests
cover concurrent submissions; the demo has no external competing bidder.

## Additional device checks

- Light theme, default font scale: full device suite passed.
- Dark theme with 1.3× font scale: all four bidding/navigation journey tests passed,
  including details content, photo restoration, and retained search on Back.
- Landscape with 1.3× text: inventory header/search remained visible; a search for
  `A-0001` survived rotation back to portrait and returned one vehicle.
- Inventory was visually checked in dark mode with larger text.
- Emulator rotation settings, font scale, and night mode were restored to their
  original values after verification.

## Practical limits

- This is not an exhaustive device matrix. API 24, API 37, physical devices,
  tablets/foldables, TalkBack, and extreme font sizes were not verified here.
- Landscape was a smoke check, not a complete landscape buyer-journey test.
- This validates the debug build. Store signing/distribution is outside scope.
- Lint passed; this report does not claim a warning-free build.
- Inventory/bids are intentionally local. External image availability and a
  server-backed auction flow are outside the guarantee.
- First-time network downloads and SDK license acceptance remain prerequisites
  on a reviewer's machine. No private environment files or credentials are needed.

Run instructions are in [README.md](../README.md); the demo is in
[DEMO.md](../DEMO.md).
