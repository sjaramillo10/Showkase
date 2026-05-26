# Key Decisions

A running log of the decisions that materially shape kronicle's path to a Compose-Multiplatform release. Each entry captures **what** was decided, **why**, and any **follow-up implications** worth flagging for future readers.

## When to add an entry

Log a decision when it changes the direction or scope of the KMP transition — what we ship, what we drop, what platforms we target, what we depend on. Skip mechanical refactors, quality-of-life improvements (catalog migration, KTS conversion, etc.), routine version bumps, and anything trivially derivable from `git log`.

Entries are listed **reverse-chronologically** (newest first). Each entry has a short stable header so it can be linked to from PRs and other docs.

---

## 2026-05 — Compose-Multiplatform target list

- **Decision**: kronicle ships for **Android**, **iOS** (`iosArm64`, `iosX64`, `iosSimulatorArm64`), **Desktop / JVM**, and **Web / Wasm** (`wasmJs`). Native targets beyond iOS (Linux/macOS/Windows) are deferred until a downstream needs them.
- **Why**: iOS is the second-largest mobile platform and the most-requested KMP target. Desktop falls out of the JVM target almost for free and broadens the addressable audience. Wasm is the modern web target with first-class Compose support since CMP 1.10. Excluding desktop natives keeps the surface area honest — most consumers don't need them.
- **Consequence**: Every `commonMain` API must work across all four targets. Anything platform-coupled (system dark-mode signals, font scale, layout direction, image loading, navigation back-stack) lives behind `expect`/`actual` boundaries. Drives several downstream choices below.
- **Tracking**: [#8](https://github.com/sjaramillo10/Showkase/issues/8), [#9](https://github.com/sjaramillo10/Showkase/issues/9), [#10](https://github.com/sjaramillo10/Showkase/issues/10), [#12](https://github.com/sjaramillo10/Showkase/issues/12).

## 2026-05 — Project rename: Showkase → kronicle  *(Phase 1.0d, pending)*

- **Decision**: Rename the fork to **kronicle**. Maven group `dev.sjaramillo.kronicle`. Java/Kotlin namespace `dev.sjaramillo.kronicle.*`. Starting version `0.0.1`.
- **Why**: The fork's direction diverges enough from upstream Showkase (Compose Multiplatform support, dropped subsystems) that a clean identity is warranted. The rename also detaches the artifacts from Airbnb's branding and Maven coordinates before any publish, removing the trademark / passing-off question entirely. The fresh `0.x` version line acknowledges the rename gives the project a distinct identity and lets pre-`1.0.0` denote an unstable API.
- **Consequence**: Public API breakage — every `@Showkase*` annotation, every `Showkase*` class, and the `Showkase` entry-point object are renamed. Acceptable at v0.x; downstream consumers re-target on adoption.
- **Tracking**: [#4](https://github.com/sjaramillo10/Showkase/issues/4).

## 2026-05 — Drop KAPT entirely  *(Phase 1.1, pending)*

- **Decision**: Remove every KAPT code path (build-script configurations, processor service descriptors, `useKsp` toggle). The Showkase processor runs KSP-only going forward.
- **Why**: KAPT is on the AGP 9 deprecation list and is incompatible with the KMP processor model. KSP2 — which we already use — is the only path to running the processor against `commonMain` across non-JVM targets. Carrying the dual-backend code path through Phases 1.2 onward would mean maintaining infrastructure we'll never ship.
- **Consequence**: The `useKsp` Gradle property goes away; consumers upgrading must migrate. Documented in the Phase 1.1 README rewrite.
- **Tracking**: [#5](https://github.com/sjaramillo10/Showkase/issues/5).

## 2026-05 — Drop all screenshot-testing modules  *(Phase 1.0c, pending)*

- **Decision**: Delete `showkase-screenshot-testing*` modules (Paparazzi + Karumi Shot), the processor's screenshot writers, and `@ShowkaseScreenshot`.
- **Why**: Both tools are Android-only — Paparazzi runs on the JVM, Shot needs an emulator. Neither translates to the iOS/Desktop/Web targets. Carrying them forward would mean either maintaining Android-only dead code through every KMP phase or building an abstraction layer that none of the targets can actually exercise. Cleaner to remove now and re-introduce a CMP-aware visual-regression tool (Roborazzi-iOS, etc.) as a fresh design if the use case returns.
- **Consequence**: kronicle 0.x ships without visual-regression support. Tracked as a Phase 7 follow-on ([#15](https://github.com/sjaramillo10/Showkase/issues/15)) for when KMP samples exist and the right tool can be chosen.
- **Tracking**: [#21](https://github.com/sjaramillo10/Showkase/issues/21). Lands **before** the rename ([#4](https://github.com/sjaramillo10/Showkase/issues/4)) so we don't waste effort renaming modules we're deleting.
