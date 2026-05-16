# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

`react-native-adpopcorn-reward` is a React Native plugin that wraps the **AdPopcorn (IGAWorks) Reward Offerwall SDK**. It is implemented as a **TurboModule** under the New Architecture (Fabric + TurboModules) — there is no legacy Bridge path.

The repo was scaffolded with `create-react-native-library@0.62.0` (`type: turbo-module`, `languages: kotlin-objc`). The current `multiply()` is template placeholder code; the real SDK surface still needs to be wired through codegen. Git status shows the pre-existing legacy Bridge sources (`RNAdPopcornRewardModule.java`, `RNAdPopcornRewardNativeAd*`) being deleted as part of this migration — do not resurrect them; new methods go through the TurboModule codegen flow described below.

Native SDK versions pinned today:

- **iOS** (CocoaPods, `AdpopcornReward.podspec`): `AdPopcornOfferwall` `5.2.3`
- **Android** (`android/build.gradle`): `com.igaworks.offerwall:AdPopcornOfferwall:9.2.6`

> Note the iOS vs Android version drift — this is the upstream vendor's reality, not a bug. Verify behavior on both platforms when touching SDK calls.

## Tooling baseline

- **Node** — version pinned in `.nvmrc` (`v24.13.0`).
- **Package manager** — **Yarn 4** (`packageManager: yarn@4.11.0`, Yarn Berry with `.yarn/`/`.yarnrc.yml`). Do **not** use npm; the workspace setup will break.
- **Workspaces** — root is the library, `example/` is the consumer app, wired together via Yarn workspaces + `react-native-monorepo-config` (see `example/metro.config.js`) and `example/react-native.config.js` (which points `react-native-adpopcorn-reward` back at the repo root with `automaticPodsInstallation: true`).
- **TS** — strict, plus `noUncheckedIndexedAccess`, `verbatimModuleSyntax`, `customConditions: ["react-native-strict-api"]`. Path alias `react-native-adpopcorn-reward` → `./src/index`.
- **Lint/format** — ESLint flat config extending `@react-native` + `prettier`. Prettier: 100 cols, semi, **double quotes** (`singleQuote: false`), trailing commas `es5`.

## Common commands

All commands are run from the repo root unless noted. CI (`.github/workflows/ci.yml`) runs `yarn lint`, `yarn typecheck`, `yarn test --maxWorkers=2 --coverage`, and `yarn npm audit --environment production`.

```sh
yarn                          # install workspace deps
yarn typecheck                # tsc (no emit)
yarn lint                     # eslint **/*.{js,ts,tsx}
yarn lint --fix               # autofix + Prettier
yarn test                     # jest (preset: @react-native/jest-preset)
yarn test path/to.test.tsx    # single file
yarn test -t "pattern"        # by test name

yarn prepare                  # build dist via react-native-builder-bob → lib/
yarn clean                    # rm lib/ + example build dirs
```

Example app (TurboModule changes require a native rebuild):

```sh
yarn example start            # Metro
yarn example ios              # iOS sim run (auto-runs pod install)
yarn example android          # Android run
yarn example build:ios        # debug build only
yarn example build:android    # gradle assemble
```

To confirm the New Architecture is live, look for `"fabric":true,"concurrentRoot":true` in Metro logs.

## Architecture

### TurboModule codegen flow

This is the load-bearing pattern — adding any native method touches all four of these files in sequence:

1. **TS spec** — `src/NativeAdpopcornReward.ts` declares `interface Spec extends TurboModule` and registers via `TurboModuleRegistry.getEnforcing<Spec>("AdpopcornReward")`. The string `"AdpopcornReward"` must match the native `moduleName`.
2. **Codegen config** — `package.json` → `codegenConfig`: `name: "AdpopcornRewardSpec"`, `jsSrcsDir: "src"`, `javaPackageName: "com.adpopcorn.adpopcornreward.reactnative"`. Codegen runs as part of the example app build and produces the spec headers/base classes.
3. **iOS impl** — `ios/AdpopcornReward.mm` declares `@interface AdpopcornReward : NSObject <NativeAdpopcornRewardSpec>` (protocol comes from the generated `<AdpopcornRewardSpec/AdpopcornRewardSpec.h>`), implements `+ moduleName` returning `@"AdpopcornReward"`, and returns a `NativeAdpopcornRewardSpecJSI` from `-getTurboModule:`.
4. **Android impl** — `android/src/main/java/com/adpopcorn/adpopcornreward/reactnative/AdpopcornRewardModule.kt` extends the generated `NativeAdpopcornRewardSpec` (abstract Kotlin class) and `AdpopcornRewardPackage.kt` registers it in a `BaseReactPackage` with `isTurboModule = true`.

If codegen output looks stale, rebuild the example app (`yarn example ios` / `yarn example android`) — the generated files live in `ios/generated/` and `android/generated/` (both git-ignored).

### Platform-conditional source files

`src/multiply.tsx` throws "only supported on native platforms"; `src/multiply.native.tsx` calls the TurboModule. Metro auto-prefers `.native.*` on iOS/Android. This pattern lets the package import cleanly in web/SSR builds and only error at call time — keep it when adding new entry points that wrap native calls.

### Build output (`yarn prepare` / `bob`)

`bob.config.js` emits two targets from `src/` into `lib/`:

- `module` (ESM) → `lib/module/`
- `typescript` (using `tsconfig.build.json`) → `lib/typescript/`

`package.json` `exports."."` advertises `source: ./src/index.tsx` (consumed in the example workspace via Metro), `types: ./lib/typescript/src/index.d.ts`, `default: ./lib/module/index.js`. `lib/` is git-ignored — never edit it.

### Versioning / publishing

`turbo.json` defines `build:android` and `build:ios` tasks with cache inputs covering native sources, but day-to-day work doesn't require Turbo. Publishing flow is GitHub Actions (`.github/workflows/publish.yml`, `release.yml`) — not invoked locally.

## Conventions specific to this repo

- **Don't add npm/lockfile churn** — Yarn Berry only. If `package-lock.json` appears, it's wrong.
- **Quotes are double, semicolons required** — matches Prettier config. ESLint will flag mixed quotes via `prettier/prettier: error`.
- **Native package name is fixed** — `com.adpopcorn.adpopcornreward.reactnative` (Android namespace + Java package + codegen). Renaming requires changes in `android/build.gradle`, `codegenConfig.android.javaPackageName`, and the Kotlin file paths/headers.
- **iOS deployment target** is `12.0` (set in podspec). Android `minSdk` is `24`, `compileSdk`/`targetSdk` `36`, Kotlin `2.0.21`, AGP `8.7.2`.
- **TurboModule name string `"AdpopcornReward"`** must stay consistent across `NativeAdpopcornReward.ts`, `ios/AdpopcornReward.mm` (`+ moduleName`), and the generated Android base class name. The Kotlin module's `NAME` companion property already references `NativeAdpopcornRewardSpec.NAME` — don't hardcode a separate string.

## Out of scope / not yet present

- No native unit tests on either platform.
- No documentation of the AdPopcorn SDK surface yet — implementation will need to mirror calls/callbacks from the IGAWorks SDK docs and decide how to expose async results (likely Promises returned from spec methods, plus `NativeEventEmitter`-style events for reward callbacks). When designing the API, sketch the TS spec first and confirm with the user before generating.
