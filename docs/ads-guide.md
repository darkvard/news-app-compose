# Ads guide: tích hợp quảng cáo (AdMob) từ A đến Z

Tài liệu học kèm theo code trong `app/src/main/java/com/example/news/ads/`. Đọc từ trên xuống dưới là nắm được toàn bộ: **có những loại ads nào, đặt ở đâu, vì sao, và code chạy ra sao**.

> Muốn tự code lại ở dự án mới? Đọc mục 1–2 để nắm bức tranh, rồi làm theo **mục 9: tự setup từ đầu, từng bước**.

---

## 1. Bức tranh tổng quan

### Tiền đến từ đâu?
- **Impression** (lượt hiển thị): app được trả theo **eCPM**, tức doanh thu trên 1000 lượt hiển thị. Phần lớn doanh thu đến từ đây.
- **Click**: một ad được click sẽ mang lại nhiều hơn. Nhưng **tuyệt đối không được** dụ hay ép người dùng click, đặt ads sát nút bấm để click nhầm, hoặc tự click ads thật của chính mình. AdMob phát hiện "invalid traffic" rất tốt, hậu quả là bị giới hạn ads hoặc **khoá tài khoản**.
- Nói gọn: **muốn tăng doanh thu thì tăng số lượt hiển thị chất lượng** (người dùng ở lại app lâu hơn, ads ít gây khó chịu hơn), chứ không phải tăng số click.

### Các loại ads và khi nào dùng
| Loại | Trông như thế nào | Dùng khi nào | Trong app này |
|---|---|---|---|
| **App Open** | Toàn màn hình, khi mở app hoặc quay lại app | Lúc người dùng đang chờ app sẵn sàng | Mở app (trong lúc splash) và quay lại từ background |
| **Interstitial** | Toàn màn hình, có nút X | Ở **điểm chuyển tự nhiên** giữa 2 nội dung | Khi mở bài báo (có giới hạn tần suất) |
| **Native** | Tự thiết kế cho giống nội dung app | Chèn vào feed / list. eCPM tốt, ít gây khó chịu | Home và Search: sau bài thứ 4, sau đó cứ mỗi 8 bài |
| **Banner** (adaptive) | Dải ngang ở đáy hoặc đầu màn | Màn người dùng ở lâu (đọc bài) | Đáy màn Detail |
| Rewarded | Video, xem xong được thưởng | Khi có phần thưởng thật (xu, mở khoá) | *Không dùng*, vì app tin tức không có phần thưởng |

### Một app thực tế cần đủ những gì? (checklist)
1. **Consent** (GDPR/CCPA) **trước** mọi request ads → `ConsentManager`
2. **Init SDK** trên thread nền, sau khi có consent → `AdsInitializer`
3. **Preload** ads toàn màn (interstitial, app open) để khi cần là có ngay
4. **Frequency capping**: giới hạn tần suất ads toàn màn → `policy/`
5. **Lifecycle**: pause/resume/destroy đúng lúc, tránh memory leak
6. **Không bao giờ bắt người dùng chờ ads** (có timeout, chưa load xong thì bỏ qua)
7. **Ads tự ẩn khi không có**: không fill hoặc không consent thì không để lại khoảng trống
8. **Test IDs** cho debug, **real IDs** chỉ dùng cho release
9. Các file và thủ tục khi phát hành: `app-ads.txt`, Data safety form, Privacy policy (xem mục 8)

---

## 2. Cấu trúc code

```
ads/
├── AdUnits.kt              ID của các ad unit (đọc từ BuildConfig)
├── AdsManager.kt           ★ Facade: phần còn lại của app chỉ nói chuyện với class này
├── AdsProvider.kt          CompositionLocal `LocalAdsManager`
├── AdsInitializer.kt       MobileAds.initialize() 1 lần, trên thread nền → isReady
├── consent/ConsentManager.kt   UMP: form consent + privacy options
├── policy/                 ★ Kotlin thuần, có unit test
│   ├── InterstitialPolicy.kt   bao giờ được hiện interstitial
│   ├── AppOpenPolicy.kt        bao giờ được hiện App Open khi quay lại
│   ├── NativeAdPlacement.kt    chèn native ở vị trí nào trong feed
│   └── Clock.kt                nguồn thời gian inject được (để test)
├── fullscreen/
│   ├── FullScreenAdTracker.kt  trạng thái chung: có ad toàn màn nào đang hiện không
│   ├── InterstitialAdManager.kt
│   └── AppOpenAdManager.kt     ProcessLifecycleOwner + theo dõi Activity hiện tại
├── nativead/
│   ├── FeedNativeAdsViewModel.kt   giữ native ad của 1 màn feed (cache + destroy)
│   ├── FeedNativeAd.kt             composable chèn vào list
│   └── NativeAdCard.kt             NativeAdView (XML) bọc trong AndroidView
└── banner/BannerAd.kt      AdView adaptive + lifecycle
res/layout/native_ad_card.xml
```

**Vì sao ads không nằm trong domain?** Ads là chuyện hạ tầng và UI, không phải nghiệp vụ "tin tức". Nếu đưa vào domain, tầng đó sẽ phải biết tới Android. Các *quy tắc* thì vẫn được tách thành class Kotlin thuần (`policy/`) để test, đúng tinh thần Clean Architecture.

**Ai được dùng ads?** Chỉ `XxxRoute` và `NewsNavigator` đọc `LocalAdsManager`. Các `XxxScreen` stateless chỉ nhận lambda hoặc slot (`nativeAds: ListInsert?`, `bottomBanner`, `onPrivacyOptionsClick`), nên preview vẫn chạy mà không cần ads.

---

## 3. Luồng khởi động (splash → consent → init → App Open)

```
MainActivity.onCreate
 └─ installSplashScreen (giữ splash cho đến khi xong cả 2 việc: đọc DataStore và AdsManager.startOnLaunch)
     AdsManager.startOnLaunch
      1. ConsentManager.gatherConsent()   → form consent (chỉ hiện nếu cần, ví dụ người dùng ở EU)
         └─ không được phép request ads → dừng: app chạy bình thường, không có ads
      2. AdsInitializer.initialize()       → MobileAds.initialize trên Dispatchers.IO (chờ tối đa 3s)
      3. Interstitial.preload()
      4. App Open cold start: load (timeout 4s) → show → nhả splash
         (bỏ qua nếu là lần chạy đầu / đang onboarding, hoặc activity được tạo lại sau khi xoay màn)
      5. AppOpen.preload()                 → có sẵn ad cho lần quay lại sau
```
> Cả chuỗi trên chạy trong `startupLock.withLock { ... }` (`Mutex`). Lý do: nếu người dùng xoay màn hình lúc form consent đang mở, Activity cũ bị huỷ và coroutine của nó bị cancel giữa chừng. Mutex bảo đảm Activity mới chạy lại cả chuỗi, còn cờ `isStartupDone` chỉ được bật khi chạy **xong thật**. (Bug này đã gặp thật: trước đây cờ được bật ngay từ đầu, nên xoay màn lúc consent là cả phiên không có ads.)

> Bài học thực tế: lần test đầu tiên, App Open khi quay lại không hiện vì **quên bước 5**. Lần quay lại đầu tiên khi đó mới bắt đầu load. Ads toàn màn phải được **preload trước** thời điểm cần hiện.

---

## 4. Chi tiết từng loại

### App Open (`AppOpenAdManager`)
- **Mở app (cold start)**: hiện trong lúc splash, với timeout. Mạng chậm thì bỏ qua, không bắt người dùng chờ.
- **Quay lại app**: `ProcessLifecycleOwner` gọi `onStart` khi *cả app* trở lại foreground (không phải mỗi khi đổi Activity). Khi đó kiểm tra `AppOpenPolicy`; đã có ad thì `show`, chưa có thì `load` cho lần sau.
- **`AppOpenPolicy` chặn App Open khi**:
  - chưa xong onboarding;
  - đang có ad toàn màn khác (người dùng click interstitial, sang trang nhà quảng cáo rồi quay lại cũng kích hoạt `onStart`!);
  - vừa share hoặc mở browser (`skipNextResume()`);
  - mới đóng ad toàn màn chưa quá 30 giây (bản debug là 0, xem `AdCooldowns`).
- **Ad hết hạn sau 4 giờ** (theo khuyến nghị của Google), sau đó phải load lại.
- **Cần một Activity để show**: theo dõi qua `ActivityLifecycleCallbacks`, bỏ qua `AdActivity` (là activity của chính SDK).

### Interstitial (`InterstitialAdManager` + `InterstitialPolicy`)
- **Gọi ở đúng 1 chỗ**: lambda `navigateToDetails` trong `NewsNavigator`. **Điều hướng trước, show ad sau**, nên khi người dùng đóng ad thì bài báo đã nằm sẵn bên dưới.
- **Quy tắc**:
  - không hiện ở lần mở bài đầu tiên, tối đa 1 lần mỗi 3 bài;
  - 2 lần hiện cách nhau ít nhất 60 giây;
  - không hiện trong vòng 30 giây sau App Open.
  - Các mốc thời gian nằm trong `AdCooldowns` (`AdUnits.kt`). Bản debug đặt hết về 0 qua cờ `ADS_NO_COOLDOWN` để test không phải chờ; release giữ giá trị thật.
- **Một object ad chỉ show được 1 lần**. Show xong là bỏ tham chiếu và **preload cái tiếp theo**.
- **Chưa load xong thì thôi, không chờ**. Lần mở bài kế tiếp vẫn đủ điều kiện để thử lại.
- **Không cho đóng bằng nút Back**: người dùng phải bấm X. Đây là hành vi của chính SDK.

### Native (`FeedNativeAdsViewModel`, `NativeAdCard`)
- **Vị trí**: `NativeAdPlacement(firstAfter = 4, interval = 8)` cho ra các index 3, 11, 19... Chuỗi: màn feed tạo `ListInsert(placement::isAdAfter) { FeedNativeAd(...) }` → `PagingArticleList(insert = ...)` → grid chèn một ô **chiếm trọn hàng** sau các index đó.
  - **Paging data không bị trộn ads vào**, ads chỉ là chuyện hiển thị.
- **Phải dùng `NativeAdView`** (một Android View) và **đăng ký từng asset view** (headline, icon, media, CTA...) thì SDK mới đếm được impression và click.
  - Trong Compose, layout XML được bọc bằng `AndroidView`.
  - Màu lấy từ `MaterialTheme`, nên tự đổi theo light/dark.
- **Bắt buộc theo chính sách AdMob**:
  - có nhãn "Ad";
  - `MediaView` tối thiểu 120×120dp;
  - không che hay sửa nội dung của ad;
  - giữ biểu tượng AdChoices (SDK tự gắn vào góc).
- **Vòng đời**: mỗi màn feed có một `FeedNativeAdsViewModel` riêng, scope theo navigation entry. Ad được cache theo slot (cuộn ngược lên không load lại) và `destroy()` hết trong `onCleared`.
  - Vì sao là ViewModel? Vì native ad phải sống **đúng bằng** thời gian màn hình tồn tại, và đó chính là việc ViewModel làm.
- **Kiểm tra**: ở bản debug, SDK hiện **"AdMob native ad validator"**. App này đã đạt *"No implementation issues found"*. Sau đó validator được tắt bằng meta-data `NATIVE_AD_DEBUGGER_ENABLED=false` trong manifest, vì nếu để bật thì mỗi native ad lại có một popup che feed. Khi sửa `native_ad_card.xml`, đổi giá trị thành `true` để kiểm tra lại.

### Banner (`BannerAd`)
- **Anchored adaptive**: rộng bằng màn hình, chiều cao do Google tính. Màn hình bình thường dùng `getLargeAnchoredAdaptiveBannerAdSize`; màn thấp (điện thoại xoay ngang, `isCompact = true`) dùng `getCurrentOrientationAnchoredAdaptiveBannerAdSize`, thấp hơn nhiều.
- **Vòng đời**:
  - `LifecycleResumeEffect` → `resume()` / `pause()` (dừng refresh khi màn không hiển thị);
  - `DisposableEffect` → `destroy()`.
- **Chỉ vẽ khi đã load xong**, nên không fill thì không chiếm chỗ.
- **Vị trí**: nằm *dưới* nội dung, không đè lên nội dung, và không sát các nút điều hướng.
- **Preview**: SDK không chạy trong Android Studio Preview, nên khi `LocalInspectionMode.current` là `true`, `BannerAd` vẽ một khung giữ chỗ cùng cỡ. `NativeAdCard` cũng có preview riêng: inflate đúng XML, dùng chung `applyTheme()` với code thật, chỉ thay nội dung bằng dữ liệu mẫu.

### Consent (`ConsentManager`, UMP SDK)
- **Bắt buộc với người dùng ở EEA/UK** (GDPR) và một số bang ở Mỹ. Google yêu cầu dùng một CMP được chứng nhận; UMP là CMP của chính Google.
- **Luồng xử lý**:
  - `requestConsentInfoUpdate` → `loadAndShowConsentFormIfRequired`;
  - sau đó kiểm tra `canRequestAds()`;
  - lỗi mạng thì vẫn dùng lựa chọn đã lưu từ lần trước.
- **Privacy options**: người dùng ở vùng bị quản lý phải đổi lại được lựa chọn. App hiện icon khiên ở header News, chỉ khi `isPrivacyOptionsRequired`.
- **Cách test**: bản debug giả lập EEA (`ADS_DEBUG_CONSENT_EEA` + `setForceTesting`). Muốn thấy lại form thì clear data của app.
- **Từ chối consent** thì `canRequestAds` có thể vẫn là `true` (ads không cá nhân hoá) hoặc `false`. Trường hợp `false` thì app không request bất kỳ ad nào.

---

## 5. Cấu hình ID (debug vs release)

`app/build.gradle.kts`:
- **debug**: luôn dùng **test IDs của Google** (`AdMobTestIds`). Các ID này luôn trả ads, an toàn để click, và có nhãn "Test Ad".
- **release**: đọc từ `local.properties` (không commit lên git):
  ```properties
  ADMOB_APP_ID=ca-app-pub-xxxxxxxx~yyyyyyyy
  ADMOB_APP_OPEN_ID=ca-app-pub-xxxxxxxx/zzzzzzzz
  ADMOB_BANNER_ID=...
  ADMOB_INTERSTITIAL_ID=...
  ADMOB_NATIVE_ID=...
  ```
  ID unit nào để trống thì định dạng đó tự tắt.
- **App ID** được đưa vào manifest qua `manifestPlaceholders["admobAppId"]`. Thiếu App ID là SDK **crash ngay khi mở app**.
- **Hai cờ meta-data trong manifest**: `OPTIMIZE_INITIALIZATION` và `OPTIMIZE_AD_LOADING` cho SDK làm việc nặng ngoài main thread.

> ⚠️ **Bẫy đã gặp khi làm**: mọi request đều trả **HTTP 403 / NO_FILL (code 3)**, kể cả với test IDs. Nguyên nhân: `applicationId` bắt đầu bằng **`com.example`**. Server quảng cáo của Google chặn package này; Play Store cũng không nhận. Cách kiểm chứng: lấy URL request trong log verbose (`adb shell setprop log.tag.Ads VERBOSE`), gửi lại nguyên URL thì nhận 403, đổi package trong URL thì nhận 200. Đã sửa bằng cách đổi `applicationId = "io.github.darkvard.news"`; namespace code vẫn là `com.example.news`.

---

## 6. Debug & test

```bash
adb logcat | grep -E "NewsAds|Ads  "         # log của app + của SDK
adb shell setprop log.tag.Ads VERBOSE         # xem cả URL request (dùng khi debug lỗi 403...)
adb shell pm clear io.github.darkvard.news    # reset consent + về lần chạy đầu
```
| Lỗi load | Ý nghĩa |
|---|---|
| code 3 `NO_FILL` | Không có ad cho request này. Với test IDs mà vẫn bị: kiểm tra package `com.example`, App ID, mạng |
| code 0 `INTERNAL_ERROR` / code 2 `NETWORK_ERROR` | Lỗi SDK / mạng |
| code 1 `INVALID_REQUEST` | Sai ad unit ID hoặc sai định dạng (ví dụ dùng ID banner cho interstitial) |

**Unit test**: `InterstitialPolicyTest`, `AppOpenPolicyTest`, `NativeAdPlacementTest` dùng `FakeClock`, nên kiểm tra được quy tắc "60 giây" mà không phải chờ thật. Các manager gọi SDK thật thì kiểm tra trên emulator, vì SDK không chạy được trên JVM.

**Kịch bản đã kiểm tra trên emulator**:
- Form consent EEA hiện ở lần mở đầu.
- Lần đầu (onboarding) không có App Open.
- Native ad ở feed qua được validator.
- Interstitial ở đúng lần mở bài thứ 3.
- Banner hiện ở Detail.
- App Open hiện khi mở lại app.

---

## 7. Hướng cải tiến (khi app lớn hơn)
Bản hiện tại đủ cho một app học tập. Khi app có người dùng thật, đây là những thứ nên thêm, theo thứ tự đáng làm:

| Cải tiến | Vì sao | Gắn vào đâu trong code |
|---|---|---|
| **Remote Config** (Firebase) cho bật/tắt và tần suất | Chỉnh `showEvery`, cooldown, tắt một định dạng mà **không cần ra bản mới** | Truyền giá trị vào constructor của `InterstitialPolicy` / `AppOpenPolicy` thay vì hằng số |
| **Tắt ads cho người dùng trả phí** | Gói Premium / IAP | Một cờ `isPremium` trong `AdsManager`: `canShowAds = isReady && !isPremium` |
| **Nhiều ad unit dự phòng** (waterfall theo giá) | Unit giá cao không có ad thì thử unit giá thấp hơn, tăng fill rate | Thay `AdUnits.xxx: String` bằng `List<String>`, manager thử lần lượt khi `onAdFailedToLoad` |
| **Mediation** (AppLovin, Meta...) | Nhiều mạng quảng cáo cạnh tranh, tăng eCPM | Cấu hình trên AdMob + thêm adapter trong Gradle, code gần như không đổi |
| **Đo doanh thu** (`OnPaidEventListener`, Firebase Analytics / Adjust) | Biết định dạng nào, vị trí nào ra tiền | Gắn listener trong từng manager khi ad load xong |
| **Màn "Loading ad" / "Welcome back"** | Che khoảng chớp màn trước khi ad toàn màn hiện | Một overlay trong `AdsProvider`, bật tắt theo `FullScreenAdTracker` |

Nhờ đã tách **policy** (quy tắc) khỏi **manager** (gọi SDK) và có một **facade** `AdsManager`, phần lớn cải tiến trên chỉ đụng 1–2 file, UI không phải sửa.

---

## 8. Checklist trước khi phát hành thật
- [ ] Tạo app và các ad unit trên AdMob, điền **real IDs** vào `local.properties` (chỉ dùng cho release).
- [ ] **Không bao giờ** tự click ads thật. Muốn test trên máy thật với real IDs thì đăng ký máy đó làm **test device** trong AdMob.
- [ ] Đặt file **`app-ads.txt`** ở domain website ghi trong Play Console.
- [ ] Cấu hình form consent (GDPR / US states) trong AdMob (Privacy & messaging).
- [ ] Play Console: khai báo **"Contains ads"**, điền **Data safety** (Advertising ID, dữ liệu do SDK ads thu thập), có **Privacy policy**.
- [ ] `applicationId` không bắt đầu bằng `com.example`.
- [ ] Xem lại tần suất ads toàn màn trên máy thật: nếu thấy phiền thì người dùng cũng thấy phiền.
- [ ] (Tuỳ chọn) Các cải tiến ở mục 7.

---

## 9. Tự setup từ đầu, từng bước

Thứ tự dưới đây đi **từ dễ đến khó**, và mỗi bước đều chạy thử được ngay. Đừng viết hết rồi mới chạy: ads lỗi thường lỗi **im lặng** (không crash, chỉ không hiện), nên càng kiểm tra sớm càng dễ tìm nguyên nhân.

| Bước | Làm gì | File | Xong khi |
|---|---|---|---|
| 0 | Chuẩn bị | `build.gradle.kts` | `applicationId` không phải `com.example.*` |
| 1 | Thêm thư viện | `libs.versions.toml`, `build.gradle.kts` | Build được |
| 2 | Cấu hình ID | `build.gradle.kts`, `AndroidManifest.xml`, `AdUnits.kt` | App mở không crash |
| 3 | Consent + init SDK | `ConsentManager`, `AdsInitializer` | Thấy form consent, log `Mobile Ads initialized` |
| 4 | Facade + nối vào app | `AdsManager`, `AdsProvider`, `MainActivity` | Splash chờ consent rồi mới vào app |
| 5 | Banner | `BannerAd.kt` | Thấy banner "Test Ad" |
| 6 | Interstitial | `InterstitialPolicy`, `FullScreenAdTracker`, `InterstitialAdManager` | Mở bài thứ 3 thì hiện |
| 7 | App Open | `AppOpenPolicy`, `AppOpenAdManager`, `NewsApplication` | Ra home rồi vào lại thì hiện |
| 8 | Native | `native_ad_card.xml`, `NativeAdCard`, `FeedNativeAdsViewModel`, `NativeAdPlacement` | Thấy card ad trong feed, validator báo không lỗi |
| 9 | Unit test các policy | `src/test/.../ads/` | `./gradlew testDebugUnitTest` pass |

### Bước 0: chuẩn bị
- `applicationId` **không** được bắt đầu bằng `com.example` (xem mục 5). Mất cả buổi debug NO_FILL nếu quên.
- Đã có Hilt (các class ads đều là `@Singleton @Inject constructor`).
- Đọc trước trang test ads của Google: https://developers.google.com/admob/android/test-ads

### Bước 1: thư viện
```toml
# libs.versions.toml
playServicesAds = "25.5.0"
ump = "4.0.0"
play-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }
user-messaging-platform = { group = "com.google.android.ump", name = "user-messaging-platform", version.ref = "ump" }
androidx-lifecycle-process = { group = "androidx.lifecycle", name = "lifecycle-process", version.ref = "lifecycleRuntimeKtx" }
```
```kotlin
// app/build.gradle.kts
implementation(libs.play.services.ads)
implementation(libs.user.messaging.platform)
implementation(libs.androidx.lifecycle.process)   // ProcessLifecycleOwner, cho App Open
```
Kiểm tra version mới nhất trên Maven Central trước khi dùng.

### Bước 2: ID theo build type
Ý tưởng: **code không bao giờ chứa ID cứng**. Gradle chọn ID theo build type rồi đưa vào `BuildConfig` (ad unit) và manifest (App ID).
```kotlin
object AdMobTestIds {            // ID mẫu của Google, đầu file build.gradle.kts
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER = "ca-app-pub-3940256099942544/9214589741"
    // APP_OPEN, INTERSTITIAL, NATIVE: xem file
}
android {
    buildFeatures { buildConfig = true }
    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = AdMobTestIds.APP_ID
            buildConfigField("String", "ADMOB_BANNER_ID", "\"${AdMobTestIds.BANNER}\"")
        }
        release {
            manifestPlaceholders["admobAppId"] = localProperties.getProperty("ADMOB_APP_ID", AdMobTestIds.APP_ID)
            buildConfigField("String", "ADMOB_BANNER_ID", "\"${localProperties.getProperty("ADMOB_BANNER_ID", "")}\"")
        }
    }
}
```
```xml
<!-- AndroidManifest.xml, trong <application> -->
<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="${admobAppId}" />
```
Rồi tạo `AdUnits` là nơi **duy nhất** đọc các ID đó (`val banner = BuildConfig.ADMOB_BANNER_ID`). ID rỗng nghĩa là tắt định dạng đó: mọi manager đều `if (AdUnits.xxx.isBlank()) return`.

**Kiểm tra**: chạy app, không crash. Thiếu App ID thì crash ngay lúc mở app, log ghi rõ.

### Bước 3: consent rồi mới init
Quy tắc vàng: **consent → init → load**. Không đảo thứ tự.
- `ConsentManager.gatherConsent(activity): Boolean`: bọc 2 callback của UMP thành **một hàm suspend** bằng `suspendCancellableCoroutine`. Đây là pattern bạn sẽ dùng lại nhiều lần, cho bất kỳ API callback nào:
  ```kotlin
  suspend fun gatherConsent(activity: Activity): Boolean = suspendCancellableCoroutine { cont ->
      fun finish() { if (cont.isActive) cont.resume(consentInformation.canRequestAds()) }
      consentInformation.requestConsentInfoUpdate(activity, params,
          { UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { finish() } },
          { finish() })   // lỗi mạng: dùng lựa chọn đã lưu, không chặn app
  }
  ```
- `AdsInitializer`: `MobileAds.initialize` trên `Dispatchers.IO`, chỉ chạy 1 lần (`AtomicBoolean`), báo xong qua `isReady: StateFlow<Boolean>`. **Mọi định dạng ads đều chờ `isReady`** trước khi load.
- Debug: giả lập EEA bằng `ConsentDebugSettings` (`DEBUG_GEOGRAPHY_EEA` + `setForceTesting(true)`) để thấy form ngay ở Việt Nam.

**Kiểm tra**: `adb shell pm clear <package>`, mở app thì thấy form; `adb logcat | grep NewsAds` thấy `Mobile Ads initialized`.

### Bước 4: facade và nối vào app
- `AdsManager` (`@Singleton`) là **cửa duy nhất**. UI không biết tới các manager bên trong. Bắt đầu chỉ với `startOnLaunch` (consent → init) và `canShowAds`; các bước sau thêm dần hàm vào đây.
- `AdsProvider` + `LocalAdsManager` (`staticCompositionLocalOf`) để Route lấy `AdsManager` mà không phải truyền qua từng màn.
- `MainActivity`:
  ```kotlin
  installSplashScreen().setKeepOnScreenCondition { !isAdsStartupDone }   // giữ splash
  lifecycleScope.launch {
      adsManager.startOnLaunch(this@MainActivity, isUserOnboarded, showAppOpenAd = savedInstanceState == null)
      isAdsStartupDone = true
  }
  setContent { NewsTheme { AdsProvider(adsManager) { NavGraph(...) } } }
  ```
- Bọc `startOnLaunch` bằng `Mutex` ngay từ đầu (xem mục 3). Test: xoay màn lúc form consent đang mở, sau đó ads vẫn phải chạy.

### Bước 5: banner, định dạng dễ nhất
Làm banner trước vì nó **chứng minh toàn bộ pipeline** (ID, consent, init) chạy đúng với ít code nhất.
- `AdView` là một Android View, nên bọc bằng `AndroidView`. Ba thứ cần có: `remember` để tạo AdView một lần và gọi `loadAd`; `LifecycleResumeEffect` để `resume()`/`pause()`; `DisposableEffect` để `destroy()`.
- Chỉ vẽ khi `onAdLoaded` đã chạy, để không có khoảng trống khi không có ad.
- Route quyết định có hiện hay không: `bottomBanner = { if (canShowAds) BannerAd() }`. Screen chỉ nhận slot, nên preview vẫn chạy.

### Bước 6: interstitial
1. Viết `InterstitialPolicy` **trước**, bằng Kotlin thuần, kèm unit test (bước 9). Đây là phần logic dễ sai nhất ("lần thứ mấy thì hiện", "cách nhau bao lâu"), nên test nó tách khỏi SDK.
2. `FullScreenAdTracker`: trạng thái chung `isShowing`, `lastDismissedAtMillis` để 2 loại ad toàn màn không chồng lên nhau.
3. `InterstitialAdManager`: vòng đời **preload → show → dismissed → preload tiếp**. Ba điều phải nhớ:
   - một object ad chỉ show được 1 lần, nên gán `interstitialAd = null` trước khi `show`;
   - chưa có ad thì bỏ qua, **không bao giờ bắt người dùng chờ**;
   - gọi sau khi `navigate(...)`, để đóng ad là thấy bài báo ngay.

### Bước 7: App Open, phần khó nhất
Khó vì có **2 lối vào** và nhiều trường hợp không được hiện.
- Cold start: `showOnColdStart(activity, timeout)` chạy trong lúc splash; dùng `withTimeoutOrNull` để mạng chậm thì bỏ qua.
- Quay lại app: `ProcessLifecycleOwner.get().lifecycle.addObserver(this)` → `onStart`. Đăng ký trong `Application.onCreate` (`NewsApplication`), không phải trong Activity.
- Cần Activity để show: theo dõi bằng `ActivityLifecycleCallbacks`, **bỏ qua `AdActivity`** của SDK.
- Các trường hợp không hiện nằm trong `AppOpenPolicy`: chưa onboarding, đang có ad khác, vừa share/mở browser (`skipNextAppOpen()`), vừa đóng ad. Ad hết hạn sau 4 giờ.
- Nhớ **preload sau khi khởi động xong**, nếu không thì lần quay lại đầu tiên không có ad (bài học ở mục 3).

**Kiểm tra**: mở app → bấm Home → mở lại app thì thấy App Open. Share bài → quay lại thì **không** thấy.

### Bước 8: native
1. Layout XML với gốc là `NativeAdView`; có nhãn "Ad", `MediaView` ≥ 120dp, icon, headline, body, CTA.
2. `NativeAdCard`: `AndroidView` inflate layout, gán nội dung, **đăng ký từng view** (`headlineView = ...`), và gọi `setNativeAd(ad)` **cuối cùng**. Màu lấy từ `MaterialTheme` để theo light/dark.
3. `FeedNativeAdsViewModel`: load bằng `AdLoader`, cache theo slot, `destroy()` tất cả trong `onCleared`. Nếu ad về sau khi màn đã đóng thì destroy ngay.
4. `NativeAdPlacement`: hàm thuần cho biết sau index nào thì chèn ad; màn feed nối vào list qua `ListInsert`.

**Kiểm tra**: bật `NATIVE_AD_DEBUGGER_ENABLED=true` (hoặc bỏ meta-data đó đi), validator phải báo *"No implementation issues found"*. Xong rồi tắt lại.

### Bước 9: test
- Policy nhận `Clock` qua constructor, nên test dùng `FakeClock` và "tua" thời gian thay vì chờ thật. Xem `InterstitialPolicyTest`.
- Code gọi SDK thì kiểm tra trên emulator theo kịch bản ở mục 6.

### Mang sang dự án khác
- **Copy được gần như nguyên**: `policy/`, `FullScreenAdTracker`, `AdsInitializer`, `ConsentManager`, `BannerAd`, khung của các manager.
- **Phải sửa theo app**: điểm gọi interstitial (ở đây là mở bài báo), vị trí native (`ListInsert`/feed), layout `native_ad_card.xml`, điều kiện "đã onboarding".
- Muốn nâng cấp tiếp: xem mục 7 (hướng cải tiến).
