# Ads guide: tích hợp quảng cáo (AdMob) từ A đến Z

Tài liệu học kèm theo code trong `app/src/main/java/com/example/news/ads/`. Đọc từ trên xuống dưới là nắm được toàn bộ: **có những loại ads nào, đặt ở đâu, vì sao, và code chạy ra sao**.

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

**Ai được dùng ads?** Chỉ `XxxRoute` và `NewsNavigator` đọc `LocalAdsManager`. Các `XxxScreen` stateless chỉ nhận lambda hoặc slot (`afterArticle`, `bottomBanner`, `onPrivacyOptionsClick`), nên preview vẫn chạy mà không cần ads.

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
  - mới đóng ad toàn màn chưa quá 30 giây.
- **Ad hết hạn sau 4 giờ** (theo khuyến nghị của Google), sau đó phải load lại.
- **Cần một Activity để show**: theo dõi qua `ActivityLifecycleCallbacks`, bỏ qua `AdActivity` (là activity của chính SDK).
- **So với SDK Lutech**: chính là `AppOpenManager` và cờ `AdsManager.isUserOutApp`.

### Interstitial (`InterstitialAdManager` + `InterstitialPolicy`)
- **Gọi ở đúng 1 chỗ**: lambda `navigateToDetails` trong `NewsNavigator`. **Điều hướng trước, show ad sau**, nên khi người dùng đóng ad thì bài báo đã nằm sẵn bên dưới.
- **Quy tắc**:
  - không hiện ở lần mở bài đầu tiên, tối đa 1 lần mỗi 3 bài;
  - 2 lần hiện cách nhau ít nhất 60 giây;
  - không hiện trong vòng 30 giây sau App Open.
- **Một object ad chỉ show được 1 lần**. Show xong là bỏ tham chiếu và **preload cái tiếp theo**.
- **Chưa load xong thì thôi, không chờ**. Lần mở bài kế tiếp vẫn đủ điều kiện để thử lại.
- **Không cho đóng bằng nút Back**: người dùng phải bấm X. Đây là hành vi của chính SDK.

### Native (`FeedNativeAdsViewModel`, `NativeAdCard`)
- **Vị trí**: `NativeAdPlacement(firstAfter = 4, interval = 8)` cho ra các index 3, 11, 19... Chuỗi `PagingArticleList` → `afterItem(index)` → `FeedNativeAd`.
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
- **Anchored adaptive**: rộng bằng màn hình, chiều cao do Google tính (`AdSize.getLargeAnchoredAdaptiveBannerAdSize`).
- **Vòng đời**:
  - `LifecycleResumeEffect` → `resume()` / `pause()` (dừng refresh khi màn không hiển thị);
  - `DisposableEffect` → `destroy()`.
- **Chỉ vẽ khi đã load xong**, nên không fill thì không chiếm chỗ.
- **Vị trí**: nằm *dưới* nội dung, không đè lên nội dung, và không sát các nút điều hướng.

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

## 7. So sánh với SDK nội bộ Lutech (`com.anonymousboiz:ads`)
| Khái niệm | Trong app này | SDK Lutech |
|---|---|---|
| Điểm truy cập chung | `AdsManager` | `AdsManager` |
| Cấp ads cho Compose | `AdsProvider` + `LocalAdsManager` | `AdsProvider` (kèm overlay Loading / Welcome back) |
| Luồng khởi động | `startOnLaunch` (consent → init → App Open) | `loadDataAndShowAds` + `getAdsEventFlow` ở Splash |
| Chặn App Open khi ra ngoài app | `skipNextAppOpen()` | `AdsManager.isUserOutApp = true` |
| Bật/tắt, tần suất | Code (`policy/`) | **Firebase Remote Config** (`isShow`, `preLoaded`, `timeOut`...): đổi không cần ra bản mới |
| Dự phòng khi không có ad | 1 ID mỗi định dạng | `unitId1 → unitId2 → unitId` (nhiều ID theo mức giá, waterfall) |
| Tắt ads cho người dùng trả phí | Không có | `BillingHelper.isUpgraded()` |
| Đo lường | Không có | Adjust (+ TikTok) để đo doanh thu ad / tracking campaign |

→ Đọc xong app này là bạn hiểu SDK Lutech đang tự động hoá những gì. Những thứ SDK đó **có thêm** chủ yếu phục vụ vận hành ở quy mô lớn: Remote Config, nhiều ID dự phòng, Premium, Adjust.

---

## 8. Checklist trước khi phát hành thật
- [ ] Tạo app và các ad unit trên AdMob, điền **real IDs** vào `local.properties` (chỉ dùng cho release).
- [ ] **Không bao giờ** tự click ads thật. Muốn test trên máy thật với real IDs thì đăng ký máy đó làm **test device** trong AdMob.
- [ ] Đặt file **`app-ads.txt`** ở domain website ghi trong Play Console.
- [ ] Cấu hình form consent (GDPR / US states) trong AdMob (Privacy & messaging).
- [ ] Play Console: khai báo **"Contains ads"**, điền **Data safety** (Advertising ID, dữ liệu do SDK ads thu thập), có **Privacy policy**.
- [ ] `applicationId` không bắt đầu bằng `com.example`.
- [ ] Xem lại tần suất ads toàn màn trên máy thật: nếu thấy phiền thì người dùng cũng thấy phiền.
- [ ] (Tuỳ chọn) Mediation để tăng fill rate và eCPM; Remote Config để chỉnh tần suất không cần ra bản mới.
