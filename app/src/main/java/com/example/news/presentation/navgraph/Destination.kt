package com.example.news.presentation.navgraph

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.example.news.domain.model.Article
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

/**
 * Type-safe navigation destinations (Navigation Compose 2.8+).
 * Each destination is a @Serializable type; destinations with arguments are data classes.
 */
sealed interface Destination {

    // Graph: app start (onboarding)
    @Serializable
    data object AppStartGraph : Destination

    @Serializable
    data object Onboarding : Destination

    // Graph: main news flow (bottom bar host)
    @Serializable
    data object NewsGraph : Destination

    @Serializable
    data object NewsNavigator : Destination

    // Bottom bar tabs + detail (inside NewsNavigator's NavHost)
    @Serializable
    data object Home : Destination

    @Serializable
    data object Search : Destination

    @Serializable
    data object Bookmark : Destination

    @Serializable
    data class Details(val article: ArticleNavArg) : Destination {
        constructor(article: Article) : this(article.toNavArg())

        companion object {
            val typeMap = mapOf(typeOf<ArticleNavArg>() to serializableNavType<ArticleNavArg>())
        }
    }
}

/** Reads the Details argument inside DetailViewModel (restored automatically after process death). */
fun SavedStateHandle.detailsArticle(): Article =
    toRoute<Destination.Details>(Destination.Details.typeMap).article.toArticle()
