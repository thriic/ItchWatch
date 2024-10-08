package com.thriic.itchwatch.ui.nav

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thriic.itchwatch.R
import com.thriic.itchwatch.ui.nav.explore.ExploreScreen
import com.thriic.itchwatch.ui.nav.explore.ExploreViewModel
import com.thriic.itchwatch.ui.nav.library.LibraryScreen
import com.thriic.itchwatch.ui.nav.library.LibraryViewModel
import com.thriic.itchwatch.ui.utils.WatchLayout


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    layoutType: NavigationSuiteType,
    layout: WatchLayout,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.EXPLORE) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            ImageVector.vectorResource(it.icon),
                            contentDescription = ""
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        },
        layoutType = layoutType,
        modifier = modifier
    ){
        when (currentDestination) {
            AppDestinations.EXPLORE -> ExploreScreen(layout, viewModel = hiltViewModel<ExploreViewModel>(), sharedTransitionScope = sharedTransitionScope, animatedContentScope =  animatedContentScope)
            AppDestinations.LIBRARY -> LibraryScreen(layout,viewModel = hiltViewModel<LibraryViewModel>(), sharedTransitionScope = sharedTransitionScope, animatedContentScope =  animatedContentScope)
            AppDestinations.SHOPPING -> {}
            AppDestinations.PROFILE -> {}
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    EXPLORE("Explore", R.drawable.search),
    LIBRARY("My Lib", R.drawable.library),
    SHOPPING("Shopping", R.drawable.search),
    PROFILE("Settings", R.drawable.settings),
}
