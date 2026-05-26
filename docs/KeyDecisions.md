# Key Decisions

A running log of non-obvious decisions made during kronicle's evolution from the Showkase fork. Each entry captures **what** was decided, **why**, and any **follow-up implications** worth flagging for future readers.

## When to add an entry

Log a decision when it:
- Picks between viable alternatives (and the reasoning isn't obvious from the code).
- Deviates from a documented plan, convention, or upstream norm.
- Introduces a workaround or temporary measure that may need to be revisited.
- Resolves an ambiguity that a future reader (including future-you) might reopen.

Skip mechanical refactors, routine version bumps, and anything trivially derivable from `git log` or `git blame`.

Entries are listed **reverse-chronologically** (newest first). Each entry has a short stable header so it can be linked to from PRs and other docs.

---

## 2026-05 — Project rename: Showkase → kronicle  *(Phase 1.0d, pending)*

- **Decision**: Rename the fork to **kronicle**. Maven group `dev.sjaramillo.kronicle`. Java/Kotlin namespace `dev.sjaramillo.kronicle.*`. Starting version `0.0.1`.
- **Why**: Detaches the fork from Airbnb's branding before any artifacts are published. Eliminates the trademark / passing-off concerns flagged when keeping the upstream name. The fresh major-zero version line acknowledges that the rename gives the project a distinct identity from upstream Showkase 1.x.
- **Consequence**: Public API breakage — every `@Showkase*` annotation, every `Showkase*` class, and the `Showkase` object are renamed. Acceptable at v0.x.
- **Tracking**: [#4](https://github.com/sjaramillo10/Showkase/issues/4).

## 2026-05 — Drop all screenshot-testing modules  *(Phase 1.0c, pending)*

- **Decision**: Delete `showkase-screenshot-testing*` modules (Paparazzi + Karumi Shot), the processor's screenshot writers, and `@ShowkaseScreenshot`.
- **Why**: Karumi Shot is dormant and on the AGP 9 incompatibility list. Paparazzi is JVM-only and has been the highest-friction surface in every preceding phase (transitive-dep issues, version pin headaches). Neither tool translates to the Compose-Multiplatform targets the project is heading toward.
- **Reversibility**: If a visual-regression need returns, reintroduce with a CMP-aware tool (Roborazzi-iOS, etc.) and a fresh design — not by un-deleting these modules.
- **Tracking**: [#21](https://github.com/sjaramillo10/Showkase/issues/21). Lands **before** the rename ([#4](https://github.com/sjaramillo10/Showkase/issues/4)) so we don't waste effort renaming modules we're deleting.

## 2026-05 — `pluginManagement` repos include `google()` and `mavenCentral()`  *(Phase 1.0b)*

- **Decision**: `settings.gradle.kts` declares `pluginManagement { repositories { gradlePluginPortal(); google(); mavenCentral() } }`.
- **Why**: Paparazzi's transitive `com.android.tools:sdk-common` only resolves from Google Maven. Without `google()` in *plugin resolution*, paparazzi-sample's build fails at configuration time.
- **Future**: Becomes obsolete once Phase 1.0c drops Paparazzi. Reconsider whether `google()` is still needed in `pluginManagement` after that lands — the `mavenCentral()` repo will still cover most plugin needs.

## 2026-05 — Detekt configuration folded into root `build.gradle.kts`  *(Phase 1.0b)*

- **Decision**: Removed the standalone `detekt/detekt.gradle.kts` script plugin. Detekt is now configured inline within the root `build.gradle.kts` `allprojects {}` block.
- **Why**: Script plugins loaded via `apply(from = ...)` run with their own compilation classpath that does not include `DetektExtension` types. The cleanest alternatives are (1) introduce `buildSrc` or precompiled-script-plugins purely to host detekt, or (2) inline. Inlining wins on simplicity at the cost of slightly less separation of concerns.
- **Tradeoff**: The root file is a few lines longer. Acceptable.

## 2026-05 — Plugin application convention: `id()` vs `alias()`  *(Phase 1.0b)*

- **Decision**: Plugins on the **buildscript classpath** (AGP, Kotlin, Karumi Shot, Vanniktech maven-publish) are applied in module scripts via plain `id("...")` with no version. Plugins resolved through the **Gradle Plugin Portal** (KSP, compose-compiler, paparazzi, detekt) use `alias(libs.plugins.*)`.
- **Why**: Mixing `alias()` (which carries a catalog-bound version) with a plugin already loaded via buildscript classpath produces `"plugin already on classpath with unknown version"` errors at configuration time. The split keeps version centralization where the catalog can handle it (plugin portal) and accepts the buildscript classpath where catalog versioning would conflict.
- **Documented in**: [CLAUDE.md](../CLAUDE.md) "Plugin application pattern" bullet.

## 2026-05 — Removed legacy KSP source-registration `afterEvaluate`  *(Phase 1.0b)*

- **Decision**: Dropped the `afterEvaluate { ... }` block in `showkase-screenshot-testing-paparazzi-sample/build.gradle` that manually registered `build/generated/ksp/<variant>/kotlin` source folders.
- **Why**: The block was a workaround for [KSP #37](https://github.com/google/ksp/issues/37), fixed in KSP 1.0.7 (June 2022). The project is on KSP 2.x, which registers generated Kotlin sources automatically; the workaround had been dead code well before the fork.
- **Reversibility**: If KSP source registration regresses, restore via the modern `LibraryExtension` API rather than reverting to the old block.
- **Note**: Moot after Phase 1.0c removes the paparazzi-sample module entirely.

## 2026-05 — Kotlin DSL throughout  *(Phase 1.0b)*

- **Decision**: All `.gradle` files converted to `.gradle.kts`. Settings, root build, every module build, and the (now-folded-in) detekt config.
- **Why**: Modern Gradle convention; better IDE tooling; type-safe accessors. Pure refactor on top of the version catalog so the change is mostly syntax.
- **Surfaced**: the plugin convention and detekt fold-in above.

## 2026-05 — Gradle version catalog as the single source of truth  *(Phase 1.0a)*

- **Decision**: `gradle/libs.versions.toml` holds every dependency coordinate, version, and plugin ID. The previous `ext.versions` / `ext.deps` Groovy maps in the root build script are removed.
- **Why**: Centralized version management with IDE auto-completion. Necessary precursor to Kotlin DSL conversion — doing both at once would have meant re-touching every dependency line twice.
- **Side change**: removed an over-broad `gradle/` rule from `.gitignore` that was hiding `libs.versions.toml` from git tracking.

## 2026-05 — Skip baseline tag; rely on CI + branch protection  *(Phase 0.1)*

- **Decision**: No `v1.0.5-pre-kmp` baseline tag was cut before migration work began.
- **Why**: Every merge to `master` requires the `Build (JDK 17)` CI check green via branch protection. A tag adds no additional safety net on a solo fork; rollback is `git revert <merge-commit>` if needed.

## 2026-05 — CI runs JDK 17 only  *(Phase 0.1)*

- **Decision**: `.github/workflows/ci.yml` runs on `ubuntu-latest` with **JDK 17 only** — no JDK 21 matrix.
- **Why**: `jvmToolchain(17)` pins the compile-time JDK, so what gets produced is JDK-17 bytecode regardless of the runner JDK. JDK 21 in CI would only exercise the Gradle daemon on a newer runtime — low ROI, doubled CI cost.
- **Reversibility**: Re-add a JDK 21 matrix entry if a concrete regression case appears.

## 2026-05 — Disable `android.yml` workflow during early migration  *(Phase 0.1)*

- **Decision**: Every line of `.github/workflows/android.yml` commented out. File left in place rather than deleted.
- **Why**: That workflow runs Paparazzi screenshot tests and emulator-based UI tests, both of which depend on infrastructure (KAPT path, AGP 8.x DSL, Karumi Shot) slated for removal or replacement in Phases 1.0c / 1.1 / 1.2. Running them now would be noise.
- **Future**: After Phase 1.0c drops screenshot testing entirely, most of `android.yml` becomes dead. Decide in Phase 1.2 whether to prune or fully reinstate (against AGP 9) — restore-by-uncomment if reinstating.

## 2026-05 — Default branch is `master`, not `main`  *(Phase 0)*

- **Decision**: Kept the upstream Showkase default branch name `master`.
- **Why**: Avoids a rename of every reference in inherited CI configs, scripts, and the issue body templates already drafted. Personal preference; not a strong technical reason either way.
- **Convention**: branches are created via `gh issue develop <n> --base master --checkout` so the auto-linked branch picks up `master` as the parent.
