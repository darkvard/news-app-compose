package com.example.news.ads.nativead

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.example.news.R
import com.example.news.ui.theme.NewsTheme
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

private object NativeAdCardDefaults {
    const val CardCornerDp = 16f
    const val MediaCornerDp = 12f
    const val IconCornerDp = 10f
    const val BadgeCornerDp = 4f
    const val ButtonCornerDp = 16f
}

/**
 * A native ad rendered with the SDK's [NativeAdView] (an Android View) inside Compose via
 * [AndroidView]. Native ads must use NativeAdView: it is what tracks impressions and clicks
 * on the registered asset views. The [NativeAd] itself is owned (and destroyed) by
 * [FeedNativeAdsViewModel], not by this composable.
 */
@Composable
fun NativeAdCard(nativeAd: NativeAd, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.native_ad_card, null) as NativeAdView
        },
        update = { view -> view.bind(nativeAd, colors) }
    )
}

private fun NativeAdView.bind(nativeAd: NativeAd, colors: ColorScheme) {
    applyTheme(colors)

    val headline = findViewById<TextView>(R.id.ad_headline)
    val advertiser = findViewById<TextView>(R.id.ad_advertiser)
    val body = findViewById<TextView>(R.id.ad_body)
    val icon = findViewById<ImageView>(R.id.ad_icon)
    val media = findViewById<MediaView>(R.id.ad_media)
    val callToAction = findViewById<TextView>(R.id.ad_call_to_action)

    // Assets (some are optional: hide the view when the ad doesn't provide them)
    headline.text = nativeAd.headline
    advertiser.text = nativeAd.advertiser
    advertiser.isVisible = !nativeAd.advertiser.isNullOrBlank()
    body.text = nativeAd.body
    body.isVisible = !nativeAd.body.isNullOrBlank()
    callToAction.text = nativeAd.callToAction
    callToAction.isVisible = !nativeAd.callToAction.isNullOrBlank()
    icon.setImageDrawable(nativeAd.icon?.drawable)
    icon.isVisible = nativeAd.icon != null
    media.mediaContent = nativeAd.mediaContent

    // Register the asset views so the SDK knows what was seen / clicked
    headlineView = headline
    advertiserView = advertiser
    bodyView = body
    iconView = icon
    mediaView = media
    callToActionView = callToAction

    // Must be the last call: it binds the ad to this view
    setNativeAd(nativeAd)
}

/** Compose theme colors -> the XML views (so the card follows light/dark). Shared with the preview. */
private fun NativeAdView.applyTheme(colors: ColorScheme) {
    val headline = findViewById<TextView>(R.id.ad_headline)
    val advertiser = findViewById<TextView>(R.id.ad_advertiser)
    val badge = findViewById<TextView>(R.id.ad_badge)
    val body = findViewById<TextView>(R.id.ad_body)
    val icon = findViewById<ImageView>(R.id.ad_icon)
    val media = findViewById<MediaView>(R.id.ad_media)
    val callToAction = findViewById<TextView>(R.id.ad_call_to_action)

    findViewById<View>(R.id.ad_container).background =
        roundedBackground(colors.surfaceContainerLow.toArgb(), NativeAdCardDefaults.CardCornerDp)
    headline.setTextColor(colors.onSurface.toArgb())
    headline.typeface = ResourcesCompat.getFont(context, R.font.poppins_semibold)
    advertiser.setTextColor(colors.onSurfaceVariant.toArgb())
    body.setTextColor(colors.onSurfaceVariant.toArgb())
    badge.setTextColor(colors.onPrimary.toArgb())
    badge.background = roundedBackground(colors.primary.toArgb(), NativeAdCardDefaults.BadgeCornerDp)
    callToAction.setTextColor(colors.onPrimary.toArgb())
    callToAction.typeface = ResourcesCompat.getFont(context, R.font.poppins_semibold)
    callToAction.background = roundedBackground(colors.primary.toArgb(), NativeAdCardDefaults.ButtonCornerDp)
    media.clipToRounded(NativeAdCardDefaults.MediaCornerDp)
    icon.clipToRounded(NativeAdCardDefaults.IconCornerDp)
}

private fun View.roundedBackground(color: Int, cornerDp: Float) = GradientDrawable().apply {
    setColor(color)
    cornerRadius = cornerDp * resources.displayMetrics.density
}

private fun View.clipToRounded(cornerDp: Float) {
    background = roundedBackground(android.graphics.Color.TRANSPARENT, cornerDp)
    clipToOutline = true
}

/**
 * Preview: a real [NativeAd] only comes from the SDK at runtime, so the preview inflates the same
 * XML, applies the same theme, and fills the views with sample text. Images are plain color blocks.
 */
@Preview(name = "light", showBackground = true)
@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun NativeAdCardPreview() {
    NewsTheme {
        val colors = MaterialTheme.colorScheme
        AndroidView(
            factory = { context ->
                LayoutInflater.from(context).inflate(R.layout.native_ad_card, null) as NativeAdView
            },
            update = { view ->
                view.applyTheme(colors)
                view.findViewById<TextView>(R.id.ad_headline).text = "Sample headline from the advertiser"
                view.findViewById<TextView>(R.id.ad_advertiser).text = "Advertiser"
                view.findViewById<TextView>(R.id.ad_body).text =
                    "Body text: a short description of the product, up to two lines."
                view.findViewById<TextView>(R.id.ad_call_to_action).text = "Install"
                view.findViewById<View>(R.id.ad_icon).apply {
                    background = roundedBackground(colors.primaryContainer.toArgb(), NativeAdCardDefaults.IconCornerDp)
                }
                view.findViewById<View>(R.id.ad_media).apply {
                    background = roundedBackground(colors.secondaryContainer.toArgb(), NativeAdCardDefaults.MediaCornerDp)
                }
            }
        )
    }
}
