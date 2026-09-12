package com.example.news.presentation.navgraph

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.news.presentation.home.HomeScreen
import com.example.news.presentation.home.HomeViewModel
import com.example.news.presentation.onboarding.OnboardingScreen

@Composable
fun NavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        navigation(
            route = Route.AppStartNavigation.route,
            startDestination = Route.OnboardingScreen.route
        ) {
            composable(route = Route.OnboardingScreen.route) {
                OnboardingScreen()
            }
        }

        navigation(
            route = Route.NewsNavigation.route,
            startDestination = Route.HomeScreen.route
        ) {
            composable(route = Route.HomeScreen.route) {
                val homeVM: HomeViewModel = hiltViewModel()
                val articles = homeVM.news.collectAsLazyPagingItems()
                HomeScreen(articles) { }
            }
//            composable(route = Route.SearchScreen.route) {
//
//            }
//            composable(route = Route.BookmarkScreen.route) {
//
//            }
//            composable(route = Route.DetailsScreen.route) {
//
//            }
        }
    }
}