# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository context

This is a personal fork of Airbnb's [Showkase](https://github.com/airbnb/Showkase) library mid-migration to Kotlin Multiplatform / Compose Multiplatform. Work is tracked on GitHub Project [Showkase KMP Migration](https://github.com/users/sjaramillo10/projects/1) with one issue per phase. Issues are labeled `phase-0-bootstrap` through `phase-7-followon`; every branch should map to one issue and one PR.

**Default branch**: `master` (not `main`). The user prefers branches created via `gh issue develop <n> --base master --checkout` so GitHub auto-links the branch to the issue.

**Commit policy** (from global rules): never add `Co-Authored-By` trailers or Claude/Anthropic attribution.

## Keep this file in sync

At the end of every session, update CLAUDE.md to match the current state of the repo. If a phase landed, drop references to the old state and update the "Build configuration" pins and architectural notes. Examples:

- After Phase 1.1 merges: remove every mention of KAPT, `-PuseKsp=true`, the `useKsp` toggle, and the dual `META-INF/services` registration. The `Codegen` paragraph should describe a KSP-only entry point.
- After Phase 1.0a merges: remove the "No version catalog yet" line and replace command examples that hard-code dependency coordinates with `libs.*` references.
- After Phase 1.0b merges: change references from `build.gradle` / Groovy DSL to `build.gradle.kts`.
- After Phase 1.0c merges: drop the "Maven group is still `com.airbnb.android`" line.
- After Phase 1.2 merges: update the AGP / Kotlin / KSP / Compose / Gradle version pins. Re-evaluate the `android.yml` commented-out status.

A future Claude reading a stale CLAUDE.md will act on stale assumptions — update aggressively rather than leaving "(soon to be removed)" footnotes.

## Verify before committing or pushing

Every task that touches the build, source, or workflow configuration must be verified locally before the commit lands. The minimum bar is the same command CI runs:

```bash
./gradlew check assembleDebug :showkase-processor:test --stacktrace
```

This must finish green before any `git commit` and before any `git push`. If a task touches a narrower surface (e.g. only the processor, or only docs), at least run that surface's tests and explain in the PR body what was skipped and why. Do not rely on CI to discover failures the local toolchain would have caught.

## Common commands

```bash
# Full local CI parity (what .github/workflows/ci.yml runs)
./gradlew check assembleDebug :showkase-processor:test --stacktrace

# Run only the annotation-processor unit tests (kotlin-compile-testing-fork)
./gradlew :showkase-processor:test

# Single processor test class / method
./gradlew :showkase-processor:test --tests "com.airbnb.android.showkase.processor.ShowkaseProcessorTest"
./gradlew :showkase-processor:test --tests "*.ShowkaseProcessorTest.someTestName"

# Build the sample app (KSP path is the supported one; the KAPT path is being removed in Phase 1.1)
./gradlew :sample:assembleDebug -PuseKsp=true

# Paparazzi screenshot tests (currently the only screenshot pipeline that runs without an emulator)
./gradlew :showkase-screenshot-testing-paparazzi-sample:verifyPaparazziDebug -PuseKsp=true

# Detekt + formatting rules (already invoked by `check`)
./gradlew detekt
```

The `-PuseKsp=true` property in `sample/build.gradle` toggles KSP vs. KAPT. KAPT is the current default; removal is tracked in issue #5 (Phase 1.1). Until that lands, prefer the KSP path for new work.

## Architecture

Showkase is an **annotation-processor library** that scans Compose `@Preview` / `@ShowkaseComposable` / `@ShowkaseColor` / `@ShowkaseTypography` annotations across modules and generates a runtime UI browser.

### Module groups

- **Annotation surface** — `showkase-annotation` defines the user-facing annotations + `ShowkaseCodegenMetadata` (intermediate annotation the processor emits on generated code so downstream modules can discover upstream-module metadata).
- **Runtime** — `showkase` contains the browser Compose UI (`ShowkaseBrowserApp`, screens under `ui/`, models under `models/`) and the public `Showkase.getBrowserIntent` entry point. Currently Android-only (`ShowkaseBrowserActivity : ComponentActivity`). Phase 3 makes this multiplatform.
- **Codegen** — `showkase-processor` is the KSP/KAPT processor. Entry points: `ShowkaseProcessorProvider` (KSP) and `ShowkaseProcessor` (KAPT). Built on **Room's XProcessing** (`androidx.room:room-compiler-processing`), which abstracts JAVAC vs. KSP. `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider` and `META-INF/services/javax.annotation.processing.Processor` register both backends. The processor walks dependency classpaths for `@ShowkaseCodegenMetadata` so the `@ShowkaseRoot` host module can aggregate previews from any number of upstream library modules.
- **Processor tests** — `showkase-processor-testing` uses `dev.zacsweers.kctfork` (kotlin-compile-testing fork) to compile fixture sources with the processor and assert on generated output.
- **Screenshot testing** — `showkase-screenshot-testing` (base API), `showkase-screenshot-testing-shot` (Karumi Shot, emulator), `showkase-screenshot-testing-paparazzi` (Paparazzi, JVM). The processor generates screenshot-test classes that iterate every component the browser would render.
- **Browser testing** — `showkase-browser-testing` + 2 submodules: end-to-end instrumentation tests across multi-module setups.
- **Samples** — `sample`, `sample-submodule`, `sample-submodule-2` demonstrate multi-module aggregation. Phase 5 replaces all three with a single `sample-kmp/` (Android + iOS + Web + Desktop).

### Codegen flow

1. Per-library-module processor pass scans `@ShowkaseComposable` / `@Preview` / `@ShowkaseColor` / `@ShowkaseTypography` and writes a `ShowkaseMetadata_<X>.kt` containing `@ShowkaseCodegenMetadata`-annotated constants.
2. In the `@ShowkaseRoot` host module, the processor reads `@ShowkaseCodegenMetadata` from the classpath (every dependency's compiled output), merges into a single `Codegen_ShowkaseBrowserProperties` class, and generates `<Root>Codegen` implementing `ShowkaseProvider`.
3. Runtime `Showkase.getBrowserIntent(context)` reflectively instantiates `<Root>Codegen` and feeds its `ShowkaseElementsMetadata` to `ShowkaseBrowserActivity`, which renders `ShowkaseBrowserApp`.

This classpath-scanning step is the central design constraint when planning the KMP migration (Phase 4): KSP2's `kspCommonMainMetadata` task pattern is what makes multi-module aggregation work across non-JVM targets.

## Build configuration

- **AGP 8.9.1, Kotlin 2.1.20, KSP 2.1.20-2.0.0, Compose 1.6.7, Gradle 8.14.2.** All bumps tracked in issue #6 (Phase 1.2).
- **No version catalog yet** — versions live as `ext.versions` / `ext.deps` Groovy maps in root `build.gradle`. Version-catalog migration is the current in-flight work (issue #2, Phase 1.0a).
- **Groovy DSL throughout** — Kotlin DSL conversion is issue #3 (Phase 1.0b), planned to follow the catalog migration.
- **Maven group is still `com.airbnb.android`** (legacy). Rename to the fork's namespace is issue #4 (Phase 1.0c).
- **Detekt** is wired across all subprojects via `detekt/detekt.gradle`. `detekt-formatting` is applied as the ktlint integration. `./gradlew check` covers it.

## CI

- **`.github/workflows/ci.yml`** is the active workflow: `./gradlew check assembleDebug :showkase-processor:test` on Ubuntu / JDK 17. Branch protection requires this green to merge into `master`.
- **`.github/workflows/android.yml`** is intentionally commented out for the early migration phases. It runs Paparazzi + emulator UI tests that depend on the KAPT path and AGP 8.x DSL; both will be reinstated or replaced in Phase 1.1 (KAPT removal) and Phase 1.2 (AGP 9 bump). Do not delete the file — restore-by-uncomment.
- **`.github/pull_request_template.md`** structures PRs with Linked issue / What changed / How to validate locally / CI status / Out-of-scope follow-ups.

## Conventions when working on migration issues

- One issue → one branch → one PR. Use `gh issue develop <n> --base master --checkout`.
- Close issues via `Closes #<n>` in the PR body.
- The PR body should match the template; surface anything that drifted from the issue scope under "Out-of-scope follow-ups" instead of expanding the PR.
- When an issue body specifies dependency versions, treat them as "verify at PR time against current releases" — the plan was written ahead of execution and version landscape may have shifted.
