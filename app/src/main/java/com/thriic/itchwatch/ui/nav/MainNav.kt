package com.thriic.itchwatch.ui.nav

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.thriic.itchwatch.R
import com.thriic.itchwatch.ui.nav.imports.Import
import com.thriic.itchwatch.ui.nav.imports.ImportViewModel
import com.thriic.itchwatch.ui.nav.explore.ExploreViewModel
import com.thriic.itchwatch.ui.nav.explore.SearchScreen
import com.thriic.itchwatch.ui.nav.library.LibraryScreen
import com.thriic.itchwatch.ui.nav.library.LibraryViewModel
import com.thriic.itchwatch.ui.nav.settings.SettingsViewModel
import com.thriic.itchwatch.ui.nav.settings.SettingsScreen
import com.thriic.itchwatch.utils.WatchLayout


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
            AppDestinations.EXPLORE -> SearchScreen(viewModel = hiltViewModel<ExploreViewModel>())
            AppDestinations.LIBRARY -> LibraryScreen(layout,viewModel = hiltViewModel<LibraryViewModel>(), sharedTransitionScope = sharedTransitionScope, animatedContentScope =  animatedContentScope)
            AppDestinations.IMPORT -> Import(layout,viewModel = hiltViewModel<ImportViewModel>())
            AppDestinations.Settings -> SettingsScreen(viewModel = hiltViewModel<SettingsViewModel>())
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    EXPLORE("Explore", R.drawable.search),
    LIBRARY("My Lib", R.drawable.library),
    IMPORT("Import", R.drawable.import_24px),
    Settings("Settings", R.drawable.settings),
}
