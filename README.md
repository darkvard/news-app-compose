# News App (Jetpack Compose)

App đọc tin tức (BBC, ABC, Google News) qua [NewsAPI](https://newsapi.org). Ban đầu dự án theo một tutorial YouTube, sau đó được refactor lại theo **Clean Architecture + MVVM + MVI**, có cache offline và bookmark.

> README này là bản đồ nhanh của dự án: đọc xong là hình dung được app làm gì, code nằm ở đâu và dữ liệu chạy ra sao.

---

## Mục lục
1. [Tính năng](#1-tính-năng)
2. [Tech stack](#2-tech-stack)
3. [Chạy dự án](#3-chạy-dự-án)
4. [Kiến trúc tổng quan](#4-kiến-trúc-tổng-quan)
5. [Cấu trúc thư mục](#5-cấu-trúc-thư-mục)
6. [Presentation: MVVM + MVI](#6-presentation-mvvm--mvi)
7. [Data: cache offline, phân trang, lỗi](#7-data-cache-offline-phân-trang-lỗi)
8. [Navigation](#8-navigation)
9. [UI conventions](#9-ui-conventions)
10. [Testing](#10-testing)
11. [Giới hạn & ghi chú](#11-giới-hạn--ghi-chú)
12. [Checklist: thêm một màn hình mới](#12-checklist-thêm-một-màn-hình-mới)

---

## 1. Tính năng

| Màn hình | Tính năng |
|---|---|
| **Onboarding** | 5 trang giới thiệu (HorizontalPager). Chỉ hiện ở lần mở app đầu tiên; trạng thái lưu bằng DataStore |
| **News (Home)** | Feed tin mới nhất, **offline-first** (đọc từ Room, tự refresh từ mạng), kéo để refresh, phân trang vô hạn (tối đa 100 bài, xem [mục 11](#11-giới-hạn--ghi-chú)), footer "You're all caught up" |
| **Search** | Tìm kiếm có debounce 400ms, bấm Search trên bàn phím để tìm ngay, nút xoá nhanh, các trạng thái: chưa nhập / không có kết quả / lỗi |
| **Bookmark** | Danh sách bài đã lưu, tự cập nhật realtime theo Room. **Nhấn giữ** để vào chế độ chọn → chọn nhiều / chọn tất cả → xoá (có dialog xác nhận + snackbar) |
| **Detail** | Ảnh, tiêu đề, nguồn • tác giả • thời gian, nội dung; lưu/bỏ bookmark (icon đúng trạng thái DB), chia sẻ, mở trình duyệt |

**Dùng chung cho cả app**
- Đủ trạng thái cho mọi màn: **loading (shimmer) / empty / error + Retry / no connection**.
- **Offline banner** khi mất mạng; có mạng lại thì **tự retry**.
- **Floating bottom bar** chỉ có icon, item được chọn có dạng pill, nội dung cuộn phía sau bị **blur** (hiệu ứng kính mờ).
- Theme tím Material 3 có **light/dark**, font Poppins cho tiêu đề.

---

## 2. Tech stack

| Nhóm | Thư viện |
|---|---|
| Ngôn ngữ / build | Kotlin 2.3.21, AGP 9.3.2, KSP, version catalog (`gradle/libs.versions.toml`) |
| SDK | minSdk 35, target/compileSdk 37 |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3, Coil (ảnh), Haze 1.7.3 (blur), SplashScreen API |
| Kiến trúc | ViewModel, Lifecycle (`collectAsStateWithLifecycle`), Hilt (DI) |
| Navigation | Navigation Compose 2.8 – **type-safe routes** (`@Serializable`) |
| Bất đồng bộ | Kotlin Coroutines + Flow |
| Mạng | Retrofit 2.11 + OkHttp (logging chỉ bật ở debug) + kotlinx.serialization |
| Lưu trữ | Room 2.8 (+ room-paging), DataStore Preferences |
| Phân trang | Paging 3 (`PagingSource`, `RemoteMediator`) |
| Test | JUnit4, kotlinx-coroutines-test, Turbine, AndroidX Test |

---

## 3. Chạy dự án

1. Lấy API key miễn phí tại https://newsapi.org.
2. Thêm vào `local.properties` (file này **không** commit lên git):
   ```properties
   NEWS_API_KEY=your_key_here
   ```
   Key được đọc vào `BuildConfig.NEWS_API_KEY` (xem `app/build.gradle.kts`) và gắn vào mọi request qua `ApiKeyInterceptor` (header `X-Api-Key`).
3. Mở bằng Android Studio rồi Run, hoặc dùng lệnh:
   ```bash
   ./gradlew :app:installDebug
   ```

**Lệnh hay dùng**
```bash
./gradlew :app:compileDebugKotlin      # build nhanh
./gradlew :app:testDebugUnitTest       # unit test (JVM)
./gradlew :app:connectedDebugAndroidTest   # instrumented test (cần emulator; GỠ app sau khi chạy)
# Chạy instrumented test mà không gỡ app (giữ dữ liệu):
./gradlew :app:installDebug :app:installDebugAndroidTest
adb shell am instrument -w com.example.news.test/androidx.test.runner.AndroidJUnitRunner
```

---

## 4. Kiến trúc tổng quan

Ba tầng của Clean Architecture. **Phụ thuộc chỉ đi vào trong**: presentation → domain ← data. Domain không biết gì về Android, Room hay Retrofit.

```
┌──────────────────────────── presentation ────────────────────────────┐
│  XxxRoute (Composable) ──Intent──▶ XxxViewModel (MviViewModel)       │
│        ▲                               │                             │
│        └────── State / Effect ─────────┘                             │
└───────────────────────────────┬──────────────────────────────────────┘
                                │ gọi use case
┌──────────────────────────── domain ───────────────────────────────────┐
│  UseCase (GetNews, ToggleBookmark, ...)                               │
│  NewsRepository (interface) · NetworkMonitor · LocalUserManager       │
│  Model thuần Kotlin: Article, Source, DataError                       │
└───────────────────────────────▲──────────────────────────────────────┘
                                │ implement
┌──────────────────────────── data ─────────────────────────────────────┐
│  NewsRepositoryImpl ─┬─ Retrofit NewsApi (DTO)                         │
│                      ├─ Room: bookmarks / cached_articles / remote_keys│
│                      └─ NewsRemoteMediator, SearchNewsPagingSource     │
│  ConnectivityNetworkMonitor · LocalUserManagerImpl (DataStore)         │
│  Mapper: DTO ⇄ Domain ⇄ Entity                                         │
└───────────────────────────────────────────────────────────────────────┘
                 di/ (Hilt) nối interface của domain với implementation của data
```

**Quy tắc giữ ranh giới giữa các tầng**
- ViewModel **chỉ** gọi use case, không inject DAO, API hay Repository trực tiếp.
- Use case chỉ đi qua interface trong `domain/` (`NewsRepository`, `NetworkMonitor`, ...).
- Mỗi tầng có model riêng: `ArticleDto` (mạng), `BookmarkEntity`/`CachedArticleEntity` (Room), `Article` (domain), `ArticleNavArg` (navigation). Chuyển qua lại bằng mapper ở ranh giới giữa các tầng.
- Lỗi mạng (Retrofit/OkHttp) được đổi thành `DataError` ngay ở tầng data, nên presentation không bao giờ thấy `HttpException`.
- Ngoại lệ có chủ đích: domain dùng `PagingData` (`paging-common` là thư viện Kotlin thuần, không phụ thuộc Android).

---

## 5. Cấu trúc thư mục

```
app/src/main/java/com/example/news/
├── NewsApplication.kt                 @HiltAndroidApp
├── di/                                Hilt modules
│   ├── NetworkModule.kt               OkHttp, Json, Retrofit, NewsApi
│   ├── DatabaseModule.kt              Room (dev: destructive fallback)
│   ├── RepositoryModule.kt            NewsRepository  ← NewsRepositoryImpl
│   └── ManagerModule.kt               LocalUserManager, NetworkMonitor
│
├── domain/                            ★ Kotlin thuần, không Android
│   ├── model/                         Article, Source, DataError
│   ├── repository/NewsRepository.kt
│   ├── manager/                       LocalUserManager, NetworkMonitor (interface)
│   └── usecase/
│       ├── news/                      GetNews, SearchNews, ObserveBookmarks,
│       │                              ObserveIsBookmarked, ToggleBookmark, DeleteBookmarks
│       ├── network/ObserveNetworkStatus.kt
│       └── app_entry/                 ReadAppEntry, SaveAppEntry
│
├── data/
│   ├── remote/                        NewsApi, NewsApiConfig, dto/, ApiKeyInterceptor,
│   │                                  ErrorMapper (Throwable → DataError), SearchNewsPagingSource
│   ├── local/                         NewsDatabase, entity/, dao/
│   ├── paging/NewsRemoteMediator.kt   cache offline cho Home
│   ├── mapper/ArticleMappers.kt
│   ├── network/ConnectivityNetworkMonitor.kt
│   ├── manager/LocalUserManagerImpl.kt   DataStore
│   └── repository/NewsRepositoryImpl.kt
│
├── presentation/
│   ├── base/MviViewModel.kt           ★ base MVVM + MVI (UiState / UiIntent / UiEffect)
│   ├── mainActivity/                  MainActivity (splash), MainViewModel
│   ├── navgraph/                      Destination, NavGraph, ArticleNavArg, SerializableNavType
│   ├── navigator/                     NewsNavigator (Scaffold + NavHost + floating bar), NavigatorViewModel
│   ├── onboarding/  home/  search/  bookmark/  detail/
│   │     └── mỗi feature: XxxContract.kt · XxxViewModel.kt · XxxScreen.kt (Route + Screen) · components/
│   └── common/                        ArticleCard, ArticleList (+ paging states), StateViews,
│                                      SearchBar, ScreenHeader, NewsButton, ErrorUi, ObserveAsEvents, ...
│
└── ui/theme/                          Color, Theme (light/dark), Type (Poppins), Shape, Spacing
```

---

## 6. Presentation: MVVM + MVI

### Ý tưởng
- **MVVM**: `ViewModel` giữ state và sống sót qua xoay màn hình. View (Compose) chỉ hiển thị.
- **MVI**: một **cổng vào duy nhất** cho mọi thao tác của người dùng (`onIntent`). State **bất biến**, chỉ đổi qua reducer (`setState { copy(...) }`). Side effect đi qua một **kênh riêng** và chỉ chạy một lần.

```
          ┌──────────── Intent (QueryChanged, ToggleBookmark, ...) ────────────┐
          │                                                                    ▼
   XxxScreen (stateless)                                         XxxViewModel.onIntent()
          ▲                                                       │ gọi use case
          │◀──────── State (StateFlow, collectAsStateWithLifecycle)┤ setState { copy() }
          │                                                       │
   XxxRoute ◀──────── Effect (Channel, ObserveAsEvents) ──────────┘ sendEffect()
   (toast, share, mở link: những việc cần Context)
```

### Base class (`presentation/base/MviViewModel.kt`)
```kotlin
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(initialState: S) : ViewModel() {
    val state: StateFlow<S>              // UI luôn vẽ từ đây
    val effects: Flow<E>                 // sự kiện 1 lần, không phát lại khi xoay màn
    abstract fun onIntent(intent: I)     // cổng vào duy nhất
    protected fun setState(reducer: S.() -> S)
    protected fun sendEffect(effect: E)
}
```
Màn nào không có Intent hoặc Effect thì truyền `Nothing`, ví dụ `MviViewModel<HomeState, Nothing, Nothing>`.

### Khuôn mỗi feature (ví dụ Bookmark)
| File | Nội dung |
|---|---|
| `BookmarkContract.kt` | `BookmarkState` (data class, có các thuộc tính tính sẵn như `isSelectionMode`), `BookmarkIntent` (sealed), `BookmarkEffect` (sealed) |
| `BookmarkViewModel.kt` | Collect `ObserveBookmarks()` trong `init` → `setState`; `onIntent` xử lý chọn/xoá |
| `BookmarkScreen.kt` | `BookmarkRoute` (lấy `hiltViewModel()`, collect state, xử lý effect) + `BookmarkScreen` (stateless, có Preview) |
| `components/` | `SelectionTopBar`, `DeleteBookmarksDialog` |

### Bảng State / Intent / Effect của từng màn
| Màn | State | Intent | Effect |
|---|---|---|---|
| Main | `isLoading`, `startDestination` | – | – |
| Onboarding | `pages` | `Finish` | – |
| Navigator | `isOnline` | – | – |
| Home | `isOnline` (+ `articles: Flow<PagingData>`) | – | – |
| Search | `submittedQuery`, `isOnline` (+ `articles`) | `QueryChanged`, `Submit` | – |
| Bookmark | `articles`, `isLoading`, `selectedUrls`, `showDeleteDialog` | `EnterSelection`, `ToggleSelection`, `ToggleSelectAll`, `ClearSelection`, `DeleteClick`, `ConfirmDelete`, `DismissDelete` | `ShowDeleted(count)`, `ShowDeleteFailed` |
| Detail | `article`, `isBookmarked` | `ToggleBookmark`, `Share`, `OpenInBrowser` | `ShowMessage`, `ShareUrl`, `OpenUrl` |

### Hai ngoại lệ có chủ đích
1. **`PagingData` không nằm trong State.** Đây là một luồng các trang do thư viện Paging quản lý (kèm `loadState` riêng), nên được expose riêng thành `val articles: Flow<PagingData<Article>>` và `cachedIn(viewModelScope)`. Thao tác refresh và retry gọi thẳng trên `LazyPagingItems`.
2. **Nội dung ô search (`query`) là `mutableStateOf` trong ViewModel.** TextField phải được cập nhật đồng bộ theo từng phím gõ; nếu đi vòng qua StateFlow (bất đồng bộ) thì con trỏ có thể bị nhảy (theo hướng dẫn của Google). Mọi giá trị suy ra từ query (`submittedQuery`, kết quả) vẫn đi qua State.

### Quyết định ở UI hay ở ViewModel?
- **Navigation thuộc về UI.** Ví dụ ở Bookmark, `BookmarkScreen` tự quyết: đang ở chế độ chọn thì tap là chọn bài, không thì mở Detail. ViewModel không biết gì về navigation.
- **Việc cần `Context`** (toast, intent share/browser) do ViewModel gửi Effect, còn `XxxRoute` thực hiện.
- **State thuần UI** (trang hiện tại của pager, vị trí cuộn) giữ ở Compose (`PagerState`, `LazyListState`).

---

## 7. Data: cache offline, phân trang, lỗi

### Room (version 1)
| Bảng | Dùng cho | Khoá |
|---|---|---|
| `bookmarks` | Bài đã lưu | `url` (PK → không trùng, xoá theo url) |
| `cached_articles` | Cache feed Home | `url`, kèm `page` + `position` để giữ đúng thứ tự |
| `remote_keys` | Trang API tiếp theo của feed | `feed` |

> Cache và bookmark nằm ở **2 bảng riêng**, nên khi refresh feed sẽ không bao giờ xoá mất bookmark.
> Dev setup: **không có migration**. Khi sửa entity, Room tự xoá và tạo lại bảng (`fallbackToDestructiveMigration`), hoặc chỉ cần clear data của app.

### Home: offline-first với `RemoteMediator`
```
UI ◀── Pager ◀── Room (cached_articles)   ← UI chỉ đọc từ Room (single source of truth)
                     ▲
     NewsRemoteMediator ── NewsApi        ← chỉ làm nhiệm vụ đổ dữ liệu vào Room
```
- `initialize()`: cache **còn mới (< 1 giờ)** thì `SKIP_INITIAL_REFRESH`, mở app là thấy ngay, kể cả khi offline. Cache cũ hơn thì refresh.
- `load(REFRESH)`: tải page 1, rồi trong một transaction xoá cache cũ và ghi cache mới.
- `load(APPEND)`: đọc `remote_keys.nextPage` để biết trang tiếp theo.
- Kéo để refresh gọi `articles.refresh()`.

### Search: chỉ dùng mạng
`SearchNewsPagingSource`: không cache, tự lọc bài trùng url giữa các trang (vì key của LazyColumn phải là duy nhất).

### Bảng trạng thái hiển thị (`PagingArticleList`)
| Tình huống | Hiển thị |
|---|---|
| Đang tải lần đầu, chưa có data | Shimmer |
| Tải lần đầu lỗi, không có data | Màn lỗi + Retry (icon và message theo loại lỗi) |
| Tải xong nhưng rỗng | Empty state riêng từng màn |
| Refresh lỗi **nhưng có cache** (Home) | Giữ danh sách + snackbar "Couldn't refresh. Showing saved news" + Retry |
| Đang tải thêm / tải thêm lỗi | Footer spinner / footer Retry |
| Hết trang | Footer "You're all caught up" / "No more results" |
| Mất mạng | Banner offline phía trên bottom bar; có mạng lại thì tự `retry()` |

### Xử lý lỗi
`data/remote/ErrorMapper.kt` đổi lỗi thành `DataError`. Sau đó `presentation/common/ErrorUi.kt` đổi `DataError` thành icon và chuỗi hiển thị.

| Exception | DataError | Hiển thị |
|---|---|---|
| `UnknownHostException`, `ConnectException`, `IOException` | `NoConnection` | No internet connection |
| `SocketTimeoutException` | `Timeout` | Server isn't responding |
| HTTP 426 / 429 | `RateLimited` | News limit reached |
| HTTP khác | `Server(code)` | Something went wrong (code) |

### Theo dõi mạng
`ConnectivityNetworkMonitor` dùng `callbackFlow` với `registerDefaultNetworkCallback`, coi là online khi có `NET_CAPABILITY_INTERNET` và `VALIDATED`. Cần quyền `ACCESS_NETWORK_STATE`.

---

## 8. Navigation

- **Type-safe** (Navigation 2.8): mỗi màn là một `@Serializable` trong `Destination.kt`.
  ```
  NavGraph (root)
  ├── AppStartGraph ── Onboarding
  └── NewsGraph ────── NewsNavigator  (Scaffold + floating bottom bar + NavHost riêng)
                         ├── Home   ┐
                         ├── Search ├ 3 tab
                         ├── Bookmark┘
                         └── Details(article: ArticleNavArg)
  ```
- **Truyền Article sang Detail**: dùng `ArticleNavArg` (bản `@Serializable` dành riêng cho navigation) cùng `serializableNavType`. Domain `Article` vì vậy không phải gắn annotation của navigation. ViewModel đọc lại bằng `savedStateHandle.detailsArticle()`, nên vẫn khôi phục được sau khi process bị kill.
- **Chuyển tab**: `popUpTo(startDestination) { saveState = true }` + `restoreState = true`, nên mỗi tab giữ nguyên vị trí cuộn. Back ở tab Search/Bookmark thì về Home.
- **Bottom bar** chỉ hiện khi đang ở một tab (kiểm tra bằng `hierarchy.any { hasRoute(...) }`), sang Detail thì trượt ẩn đi.
- **Start destination** do `MainViewModel` quyết định dựa trên DataStore (đã xem onboarding hay chưa).

---

## 9. UI conventions

- **Màu**: chỉ dùng `MaterialTheme.colorScheme.*` (định nghĩa trong `ui/theme/Color.kt` và `Theme.kt`). Không dùng `colors.xml` hay `colorResource`.
- **Chữ**: `MaterialTheme.typography.*`. Heading và title dùng Poppins, body dùng font hệ thống.
- **Bo góc**: `MaterialTheme.shapes.*` (`medium` 12dp cho ảnh card, `large` 16dp cho search bar, ảnh detail và button).
- **Khoảng cách chung**: `Spacing` (`ExtraSmall` 4 · `Small` 8 · `Medium` 12 · `Large` 16 · `ExtraLarge` 24 · `Huge` 40 · `ScreenHorizontal` 20).
- **Kích thước riêng của từng component** đặt trong `private object XxxDefaults` ở **đầu file** composable, ví dụ:
  ```kotlin
  object ArticleCardDefaults {
      val ContentPadding = 8.dp
      val ImageSize = 96.dp
      const val TitleMaxLines = 3
  }
  ```
  → Không hard-code số `dp` rải rác trong code UI; muốn chỉnh thì sửa ở một chỗ duy nhất.
- **Text**: toàn bộ để trong `res/values/strings.xml` (dùng `plurals` khi có số lượng).
- **Preview**: mỗi `XxxScreen` đều stateless nên preview được với state giả (có cả light/dark).
- **Edge-to-edge**: các màn tự `statusBarsPadding()`. Nội dung chạy xuống dưới floating bar; list nhận `bottomPadding` để item cuối vẫn cuộn lên trên được thanh bar.

---

## 10. Testing

| Loại | File | Kiểm tra |
|---|---|---|
| Unit (JVM) | `data/ArticleMappersTest` | DTO, Domain, Entity; lọc bài không có url / `[Removed]` |
| Unit | `data/ErrorMapperTest` | Exception → `DataError`; logic hết trang (giới hạn 100 bài) |
| Unit | `presentation/SearchViewModelTest` | Debounce, Submit tìm ngay và không bị lặp, xoá query |
| Unit | `presentation/BookmarkViewModelTest` | Chế độ chọn, chọn tất cả, xoá có xác nhận, dismiss, tự loại url không còn tồn tại khỏi selection |
| Unit | `presentation/ArticleNavArgTest` | Mapping nav arg ⇄ Article |
| Instrumented | `data/BookmarkDaoTest` | Upsert không tạo trùng, `observeIsBookmarked`, `deleteByUrls` |
| Instrumented | `presentation/DetailViewModelTest` | Đọc nav arg, toggle bookmark + message, effect share/open |

Fake dùng chung cho unit test: `test/.../fake/FakeNewsRepository`, `FakeNetworkMonitor`. Rule `MainDispatcherRule` thay `Dispatchers.Main` khi test.

> `DetailViewModelTest` là instrumented test vì `SavedStateHandle.toRoute()` cần `Bundle` thật của Android.

---

## 11. Giới hạn & ghi chú

- **Gói Developer (free) của NewsAPI chỉ trả tối đa 100 kết quả mỗi truy vấn.** Trang thứ 6 (với pageSize 20) trả HTTP 426 `maximumResultsReached`, nên feed dừng ở 100 bài và hiện footer "You're all caught up". Muốn cuộn nhiều hơn thì cần key trả phí, rồi chỉnh `NewsApiConfig.MAX_RESULTS`.
- Gói free còn giới hạn **số request mỗi ngày**; khi vượt sẽ hiện màn "News limit reached".
- `content` của NewsAPI bị cắt ngắn ("… [+1131 chars]"). Màn Detail bỏ đoạn đuôi này và có nút **Read full article** để mở bài gốc.
- **API key** để trong `local.properties`. Key cũ từng bị commit vào lịch sử git, nên hãy dùng key mới.
- **Haze dùng bản 1.7.x** vì bản 2.x cần Kotlin 2.4.

---

## 12. Checklist: thêm một màn hình mới

1. **Domain**: thêm use case vào `domain/usecase/...`. Nếu cần dữ liệu mới thì mở rộng interface `NewsRepository`.
2. **Data**: implement trong `NewsRepositoryImpl` (thêm DAO/API/mapper nếu cần).
3. **Contract**: `presentation/feature/FeatureContract.kt` gồm `FeatureState : UiState`, `FeatureIntent : UiIntent`, `FeatureEffect : UiEffect`.
4. **ViewModel**: `@HiltViewModel class FeatureViewModel @Inject constructor(useCases...) : MviViewModel<FeatureState, FeatureIntent, FeatureEffect>(FeatureState())`
   - Collect dữ liệu từ Flow trong `init`, đổ vào state bằng `setState { copy(...) }`.
   - Xử lý thao tác người dùng trong `onIntent`; việc cần `Context` thì gửi bằng `sendEffect`.
5. **UI**: `FeatureScreen.kt`
   - `private object FeatureDefaults { ... }` ở đầu file.
   - `FeatureRoute(...)`: `hiltViewModel()`, `collectAsStateWithLifecycle()`, `ObserveAsEvents(viewModel.effects)`.
   - `FeatureScreen(state, onIntent, ...)`: stateless, có `@Preview`.
6. **Navigation**: thêm `@Serializable` vào `Destination`, rồi thêm `composable<Destination.Feature> { FeatureRoute(...) }` vào NavHost tương ứng.
7. **Strings**: đưa text vào `strings.xml`.
8. **Test**: viết unit test cho ViewModel (dùng fake repository + `MainDispatcherRule` + Turbine).
