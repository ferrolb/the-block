# OPENLANE-inspired appearance

Reviewed on October 30, 2026:

- https://www.openlane.com/
- https://play.google.com/store/apps/details?id=com.backlotcars.backlotcars&hl=en_US

The website's computed styles use `#0061FF` for blue actions, `#0A1B5F`
for navy headings, Poppins typography, and rounded calls to action. The Play
Store screenshots reinforce white inventory surfaces, blue accents, neutral
separators, and clear vehicle/price hierarchy. This implementation adapts those
cues to The Block's existing native buyer flow; it is not an official design kit.

## Application

- Shared Material 3 light and dark color schemes replace the default purple and
  Android wallpaper-derived colors. Night colors are an adaptation of the brand
  palette, not a claim about OPENLANE's official dark theme.
- Poppins regular, medium, semibold, and bold are bundled locally for every
  typography role. Fonts come from the Google Fonts repository:
  https://github.com/google/fonts/tree/main/ofl/poppins
  The redistribution license is in `licenses/Poppins-OFL.txt`.
- White inventory cards have subtle borders, navy lot badges, blue prices,
  pale blue bid-count pills, and a separated price footer. The header retains
  The Block identity with an OPENLANE text label and a rounded search field.
- Details use matching section cards and a neutral app bar. The pinned bid
  action is at least 52 dp tall, uses the primary action color, and respects
  the same 720 dp content width as the screens. Dialog primary actions are filled.
- Launch-window backgrounds and native accent colors follow the brand in both
  system modes. Search, bidding rules, data, and navigation are unchanged.

## Verification

Build, unit tests, and lint:

```sh
./gradlew :app:assembleDebug :app:lintDebug :vehicle:ui:lintDebug :vehicle:ui:testDebugUnitTest :app:testDebugUnitTest
```

Emulator checks:

```sh
./gradlew :app:connectedDebugAndroidTest
```

`AppearanceJourneyTest` exercises inventory, vehicle navigation, and opening and
cancelling a bid in light mode, dark mode, and at 150% text size. It records PNGs
under the debug app's private `files/appearance/` directory for visual inspection.
The existing tests cover the confirmed bid journey, navigation restoration,
search, loading, error, and empty states.

Verified on a Pixel 7 emulator running API 36: all 16 instrumentation tests
passed. Debug assembly, app/UI lint, and app/UI unit tests passed as well.
The appearance checks also run directly with the Android test runner so the
app remains installed and the captures can be retrieved after the run. Gradle's
connected-test task uninstalls its test application after completion.
