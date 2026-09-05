package com.example.news.presentation.onboarding

import androidx.annotation.DrawableRes
import com.example.news.R

data class Page(
    val title: String,
    val description: String,
    @DrawableRes val image: Int
) {

    companion object {
        private val images = listOf(
            R.drawable.img_ob_1,
            R.drawable.img_ob_2,
            R.drawable.img_ob_3,
            R.drawable.img_ob_4,
            R.drawable.img_ob_5,
        )

        fun getPages(): List<Page> = images.map { image ->
            Page(
                title = "Lorem Ipsum is simply dummy",
                description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                image = image,
            )
        }
    }

}

