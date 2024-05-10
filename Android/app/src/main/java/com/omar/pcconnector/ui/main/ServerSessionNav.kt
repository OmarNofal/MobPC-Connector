package com.omar.pcconnector.ui.main

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument


fun NavGraphBuilder.serverUI() {

    navigation(
        "home",
        "server/id",
        listOf(
            navArgument("id") {
                type = NavType.StringType
            }
        )
    ) {

        composable("home") {
        }

        composable("image") {

        }

    }

}