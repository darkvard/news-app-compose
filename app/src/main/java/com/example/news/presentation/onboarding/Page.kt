package com.example.news.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.news.R

data class Page(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val image: Int
) {
    companion object {
        /** One page per main feature of the app. */
        val all: List<Page> = listOf(
            Page(R.string.onboarding_title_1, R.string.onboarding_desc_1, R.drawable.img_ob_1),
            Page(R.string.onboarding_title_2, R.string.onboarding_desc_2, R.drawable.img_ob_2),
            Page(R.string.onboarding_title_3, R.string.onboarding_desc_3, R.drawable.img_ob_3),
            Page(R.string.onboarding_title_4, R.string.onboarding_desc_4, R.drawable.img_ob_4),
            Page(R.string.onboarding_title_5, R.string.onboarding_desc_5, R.drawable.img_ob_5),
        )
    }
}
