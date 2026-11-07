package com.example.news.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),   // article images, cards
    large = RoundedCornerShape(16.dp),    // search bar, detail image, buttons
    extraLarge = RoundedCornerShape(28.dp),
)
