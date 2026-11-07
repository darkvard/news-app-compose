package com.example.news.presentation.navgraph

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.news.presentation.navigator.NewsNavigator
import com.example.news.presentation.onboarding.OnboardingRoute

/** Root graph: onboarding (first launch) or the main news flow. */
@Composable
fun NavGraph(startDestination: Destination) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        navigation<Destination.AppStartGraph>(startDestination = Destination.Onboarding) {
            composable<Destination.Onboarding> {
                OnboardingRoute()
            }
        }

        navigation<Destination.NewsGraph>(startDestination = Destination.NewsNavigator) {
            composable<Destination.NewsNavigator> {
                NewsNavigator()
            }
        }
    }
}
