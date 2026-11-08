# News App

An Android news reader built with Jetpack Compose, powered by [NewsAPI](https://newsapi.org).
Clean Architecture + MVVM + MVI, offline-first feed, bookmarks and AdMob ads with GDPR consent.

## Screenshots

All ads are Google test ads.

**Portrait · light**

<table>
<tr><td align="center"><img src="https://github.com/user-attachments/assets/604345fe-b4ef-4718-86b9-a0f61207c8c0" width="200"><br><sub>GDPR consent (UMP)</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/07f4bcc4-46a4-4021-9257-4fcfe32ac739" width="200"><br><sub>Onboarding</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/4df8f649-5b71-4edd-aab0-99166fdfb8ca" width="200"><br><sub>Feed + native ad</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/a1de7933-4cb8-435e-badb-f40395145fac" width="200"><br><sub>Detail + banner, bookmark saved</sub></td></tr>
<tr><td align="center"><img src="https://github.com/user-attachments/assets/78eb1f4f-ae1e-4f03-9f76-cd8885d98d31" width="200"><br><sub>Interstitial (3rd article)</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/acbdd09a-0b9b-4b7c-b7d5-160a17c56ffd" width="200"><br><sub>App Open (back to app)</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/bfa4eadb-a04c-42ad-adc5-ee12a376616e" width="200"><br><sub>Search: empty prompt</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/adc36fbf-08ea-41ce-9bef-be450213b73e" width="200"><br><sub>Search results + native ad</sub></td></tr>
<tr><td align="center"><img src="https://github.com/user-attachments/assets/6aa35a0f-2055-4c2d-b454-eca1cd055724" width="200"><br><sub>Search: no results</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/d0f6c07b-70f7-43aa-921d-a838e0cf90da" width="200"><br><sub>Multi-select delete</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/2e5c9826-af8f-4b98-b0bd-2be078d72856" width="200"><br><sub>Bookmarks: empty</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/9e8ef77c-8e73-4058-9fd5-ae4f15a8d8d4" width="200"><br><sub>Offline: no connection</sub></td></tr>
</table>

**Landscape · dark**

<table>
<tr><td align="center"><img src="https://github.com/user-attachments/assets/d73e8e62-cb25-4b11-ba15-3df86c5dbf0d" width="400"><br><sub>Onboarding: two panes</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/e8ecd50e-7fbd-4297-8077-b59bb1cdcfaa" width="400"><br><sub>Navigation rail + 2-column grid</sub></td></tr>
<tr><td align="center"><img src="https://github.com/user-attachments/assets/35d03f22-3b7a-4ade-b7e1-2b8886a3e363" width="400"><br><sub>Detail: two panes + banner</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/38b995d9-53ec-4c3b-ada2-065fe11bc689" width="400"><br><sub>Search: one-row header</sub></td></tr>
<tr><td align="center"><img src="https://github.com/user-attachments/assets/7a3fe699-626f-44ea-85f4-111eff69880c" width="400"><br><sub>Multi-select delete</sub></td><td align="center"><img src="https://github.com/user-attachments/assets/bd56de57-56cf-44c6-8f52-72780b9d31cb" width="400"><br><sub>Offline</sub></td></tr>
</table>

## Features

| Screen | What it does |
|---|---|
| Onboarding | 5-page intro, shown only on first launch (DataStore) |
| News | Offline-first feed (Room cache, 1h freshness), pull to refresh, infinite scroll |
| Search | Debounced search, paginated results |
| Bookmark | Saved articles, long-press to multi-select and delete |
| Detail | Article view, bookmark, share, open in browser |
| Ads | UMP consent, App Open, Interstitial, Native in feed, Banner on Detail ([guide](docs/ads-guide.md)) |

Every list handles loading (shimmer), empty, error + retry and offline states. An offline banner appears when the network drops, and the feed retries automatically when it's back.

## Tech stack

Kotlin 2.3 · Jetpack Compose (Material 3) · Hilt · Navigation Compose (type-safe routes) · Coroutines + Flow · Retrofit + OkHttp + kotlinx.serialization · Room · Paging 3 (`RemoteMediator`) · DataStore · Coil · Haze (blur) · Google Mobile Ads + UMP · JUnit, Turbine

minSdk 35, targetSdk 37, AGP 9.3. Versions live in `gradle/libs.versions.toml`.

## Getting started

1. Get a free API key at [newsapi.org](https://newsapi.org).
2. Add it to `local.properties` (not committed):
   ```properties
   NEWS_API_KEY=your_key
   ```
3. Run:
   ```bash
   ./gradlew :app:installDebug
   ```

Debug builds use Google's test ad units, so no AdMob setup is needed. Release builds read real IDs from `local.properties`
(`ADMOB_APP_ID`, `ADMOB_APP_OPEN_ID`, `ADMOB_BANNER_ID`, `ADMOB_INTERSTITIAL_ID`, `ADMOB_NATIVE_ID`); an empty unit ID turns that format off.

```bash
./gradlew :app:testDebugUnitTest          # unit tests
./gradlew :app:connectedDebugAndroidTest  # instrumented tests (emulator required)
```

## Architecture

```
presentation  ──▶  domain  ◀──  data
(Compose, VM)     (pure Kotlin)  (Retrofit, Room, DataStore)
```

- **domain**: models (`Article`, `DataError`), the `NewsRepository` interface and use cases. No Android dependencies.
- **data**: implements the domain interfaces. DTOs and entities are mapped to domain models, and exceptions are mapped to `DataError` at the boundary.
- **presentation**: ViewModels call use cases only. They never touch a DAO, an API or the repository directly.
- **di**: Hilt modules that bind domain interfaces to data implementations.
- **ads**: platform code, deliberately kept out of the domain layer.

**MVVM + MVI.** Every ViewModel extends `presentation/base/MviViewModel<State, Intent, Effect>`:

```kotlin
val state: StateFlow<S>           // the UI renders from this
val effects: Flow<E>              // one-off events: snackbar, share, open URL
fun onIntent(intent: I)           // single entry point for user actions
protected fun setState(reducer: S.() -> S)
```

The Paging stream is the exception: `Flow<PagingData>` is exposed separately from `State`.

**Offline feed.** The Home list reads only from Room. `NewsRemoteMediator` fills Room from the API and skips the refresh while the cache is less than 1 hour old.

## Project structure

```
app/src/main/java/com/example/news/
├── di/              Hilt modules
├── domain/          model/, repository/, manager/, usecase/
├── data/            remote/, local/ (Room), paging/, mapper/, repository/
├── ads/             AdsManager, consent/, policy/, fullscreen/, nativead/, banner/
├── presentation/
│   ├── base/        MviViewModel
│   ├── navgraph/    Destination (type-safe routes), NavGraph
│   ├── navigator/   NewsNavigator: tabs + floating bottom bar
│   ├── common/      shared composables (ArticleList, StateViews, ...)
│   └── <feature>/   XxxContract.kt · XxxViewModel.kt · XxxScreen.kt · components/
├── ui/adaptive/     WindowLayout: rail vs bottom bar, compact height
└── ui/theme/        colors, typography, shapes, Spacing
```

## Adding a feature

1. **Domain**: add a use case; extend `NewsRepository` if new data is needed.
2. **Data**: implement it in `NewsRepositoryImpl` (DAO, API, mapper).
3. **Contract**: `FeatureContract.kt` with `FeatureState`, `FeatureIntent` and `FeatureEffect`.
4. **ViewModel**: `@HiltViewModel` extending `MviViewModel`. Handle actions in `onIntent` and update state with `setState { copy(...) }`.
5. **UI**:
   - `FeatureRoute`: gets the ViewModel and collects state and effects.
   - `FeatureScreen`: stateless, with a `@Preview`.
6. **Navigation**: add a `@Serializable` destination to `Destination` and register it in the NavHost.
7. **Tests**: write a ViewModel unit test with `FakeNewsRepository` and `MainDispatcherRule`.

**UI conventions**
- Colors and typography come only from `MaterialTheme`.
- Use `Spacing` for shared spacing, and a `private object XxxDefaults` at the top of a file for component-specific sizes.
- Put all text in `strings.xml`.

**Adaptive layout (portrait, landscape, tablet)**

Pick the layout from the window size, never from the orientation: `rememberWindowLayout()` in `ui/adaptive/`.

| When | Rule |
|---|---|
| `useNavigationRail` (short or wide window) | Tabs move from the bottom bar to a rail on the left (`NewsNavigator`) |
| Lists | `ArticleGrid` uses adaptive columns (min 340dp): 1 column on a phone in portrait, 2 in landscape. Native ads and footers take a full row |
| `isCompactHeight` (phone in landscape) | Save height: smaller headers, side-by-side panes (Detail, Onboarding), shorter banner and native media (`values-h480dp`) |

For a new screen: check it in landscape with `@Preview(device = LANDSCAPE_PHONE_PREVIEW)`. If it is a list, reuse `ArticleGrid`. If it is a detail or long-content screen, split it into two panes when `isCompactHeight`. Rotation must not reload data: keep state in the ViewModel and use `cachedIn` for paging.

## Notes

- The NewsAPI free plan caps each query at **100 results**. The list stops there and shows an end-of-list footer.
- `applicationId` is `io.github.darkvard.news`, while the code namespace stays `com.example.news`. Google's ad servers reject `com.example.*` packages.
- Room has no migrations, which is fine for a dev project: schema changes recreate the tables.
- Haze stays on 1.7.x because 2.x requires Kotlin 2.4.
